package net.personaltt.simplesolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.ProblemDefinition;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;
import net.personaltt.utils.IntervalMultimap;
import net.personaltt.utils.IntervalMultimap.MultiIntervalStop;

/**
 * Simple solver finds any feasible schedule for personal timetabling problem.
 * @author docx
 */
public class SimpleSolver {

    /**
     * Working schedule keeps working state of shedule during solver run
     */
    private Schedule workingSchedule;
    
    /**
     * Timeout in ms after which solver stops
     */
    long timeoutLimit = 15000;
    
    /**
     * Instance of random used in solver
     */
    Random random;
    
    public SimpleSolver() {
        random = new Random();
    }
    
    public SimpleSolver(Random random) {
        this.random = random;
    }
    
    
    // NOTES:
    //
    // Consider this situation
    // Two occureances with domains (# for min dur, XX for max dur, - for domain:
    // |#####XX---|
    // |---XX#####|
    // Cannot be solved by any simple operation of each occurrence
    // It needs to tend to "minimal conflict" even inside conflict
    //
    // If algo choose first, and removes allocation of others from its domain it gets
    // |---|
    // Simillary for the second one
    //        |---|
    // It can be choosen minimal duration (as always it will grab Max(min_dur, Min(max_dur,space_dur)))
    // But where to place start? To the left or to the right?
    // First it has to place it inside its domain. So graph would be extended to
    // |===------| for first
    // |------===| for second
    // where = is free space and - is domain
    // It can try to align to center of free space. 
    //
    // L: Free space is always surrounded by other allocation or by edge of domain
    // P: Given freespace s, if is continuous domain on the right (resp on the left)
    //    of s, there must be other allocation, becuase freespace is domain - allocations.
    //    Otherwise domain ends on the right (resp left) edge of s. QED
    //
    // So with this lemma when we align minimum duration on occurrence
    // to the center of the freespace it has the same effect as to the right or the left
    
    
    /**
     * Solves given problem. It is trying to find feasible solution (solution with
     * minimal conflicting area). Does no optimalization.
     * 
     * 
     * @param problem
     * @return solution
     */
    public Schedule solve(ProblemDefinition problem) {
        // clones initial schedule from problem definition
        workingSchedule = (Schedule)problem.initialSchedule.clone();
        
        //TODO solver assumes that domain of all occurrences has all it intervals
        // large enought to at least minimal duration of occurrence fits in it.
        // If its cannot be ensured, these intervals smaller than minimal duration
        // can be dropped in preprocessing
        
        // Prepare multimap of intervals of occurrences in initial schedule
        IntervalMultimap<Integer, Occurrence> currentAllocationIntervals = 
                new IntervalMultimap<>();
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : workingSchedule.getOccurrencesAllocations()) {
            currentAllocationIntervals.put(entry.getKey(), entry.getValue().toInterval());
        }
        
        // get conflicting occurrences list from multimap
        List<Occurrence> conflictingOccurrences = currentAllocationIntervals.valuesOfOverlappingIntervals();
        
        // Start timer
        long startTime = System.currentTimeMillis();
        long iteration = 0;
        
        // while there is conflict, solve it
        while(conflictingOccurrences.size() > 0 && System.currentTimeMillis() - startTime < timeoutLimit) {
            System.out.printf("Iteration %s, %s conflicts\n", iteration, conflictingOccurrences.size());
            
            // Get random conflicting occurrence and its current allocation
            Occurrence toSolve = conflictingOccurrences.get(random.nextInt(conflictingOccurrences.size()));
            OccurrenceAllocation solvingAllocation = workingSchedule.getAllocationOf(toSolve);
           
            System.out.printf("Selected occurrence %s at %s\n", toSolve, solvingAllocation);
            
            // Remove solving occurrence from allocation map
            // So in the map is only "other" allocations 
            currentAllocationIntervals.remove(toSolve);
            
            // allocationIntervals = ordered list of triples t_i time, n_i N, d_i bool, 
            // so that in interval t_i - t_i+1 there is n_i allocated occurences in 
            // currentAllocationIntervals and union of all t_i - t_i+1 where d_i = true
            // is equal to domain of toSolve.
            List<IntervalMultimap<Integer,Occurrence>.MultiIntervalStop> allocationIntervals = 
                    currentAllocationIntervals.getIntervalsIn(toSolve.getDomain());
            
            
            // find all allocations in domain of toSolve, which are
            // minimal in area of conflict by allocationInterals
            // and then maximal in duration
            List<OccurrenceAllocation> bestAllocations = findBestAllacations(
                    allocationIntervals, 
                    toSolve
                    );
            System.out.printf("No. of found best possible allocations: %s \n", bestAllocations.size());
            
            
            // select random allocation from list of best allocations
            solvingAllocation.set(bestAllocations.get(random.nextInt(bestAllocations.size())));
            
            // store in allocation intervals. if it conflicts with existing intervals, 
            // it returns true
            boolean conflicts = currentAllocationIntervals.put(toSolve,solvingAllocation.toInterval());  
            
            System.out.printf("Resolved as: %s With conflict: %s\n", solvingAllocation, conflicts);
            
            // retrieve new list of conflicting occurrences
            conflictingOccurrences = currentAllocationIntervals.valuesOfOverlappingIntervals();
            iteration++;
        }
        
        return workingSchedule;
        
    }
    
    
    // TODO this is not correct algo:
    // Consider situation 
    //    1     3   0    allocations
    // |------|---|---|
    // 0      6   9   12
    // 
    // and duration of occurrence is 9 - 9
    // this algo founds "best" as
    // 
    // 0          9
    // *==========* 
    // which is 6*1 + 3*3 = 15 conflict area
    // 
    // but
    //    3           12
    //    *===========*
    // has 3*1+3*3+3*0 = 12 area 
    // and is the same longest.
    
    /**
     * Find all allocations that are smallest in conflict area and then largest in duration
     * in subject of valid allocation for occurrence
     * @param intervals Ordered list of times in which count of allocated occurrences is changes and also definining occurrence domain
     * @param occurrence Occurrence which is subject to finding allocation
     * @return 
     */
    private List<OccurrenceAllocation> findBestAllacations(List<IntervalMultimap<Integer,Occurrence>.MultiIntervalStop> intervals, Occurrence occurrence) {
        // Initialization
        ArrayList<OccurrenceAllocation> best = new ArrayList<>();
        int bestConflict = Integer.MAX_VALUE;
        int bestDuration = 0;
        
        // Go throught all changes (stops) starts        
        for (int i = 0; i < intervals.size(); i++) {
            IntervalMultimap<Integer, Occurrence>.MultiIntervalStop startStop = intervals.get(i);
            
            // if change is to "outside intervals", continues
            if (startStop.isIn == false) {
                continue;
            }
            
            int start = startStop.stop;
            int conflict = 0;
            int duration = 0;
            int j = i ;
            
            IntervalMultimap<Integer, Occurrence>.MultiIntervalStop endStop = startStop;
            
            // go now throught all stops j after start stop i, so that 
            // t_j - t_j+1 contains possible duration of occurrence (exists 
            // dur in min_dur - max_dur so that t_i + dur is in t_j + t_j+1)
            // and until t_j - t_j+1 is inside domain (until first j that is_in_j = false)
            while(++j < intervals.size() && endStop != null && endStop.isIn) {   
                // if duration is now maximal, it is cropped by previous interval larger, so
                // end
                if (duration == occurrence.getMaxDuration()) {
                    break;
                }
                IntervalMultimap<Integer, Occurrence>.MultiIntervalStop currentStart = endStop;
                
                endStop = intervals.get(j);
                
                // if current interval contains minimal duration, set duration to minimal and
                // keep this interval for next interation also
                int prevDuration = duration;
                if (duration < occurrence.getMinDuration() && (endStop.stop - start) > occurrence.getMinDuration()) {
                    duration = occurrence.getMinDuration();
                    j--;
                    endStop = currentStart;
                } else {
                    duration = endStop.stop - start;
                }
                
                // add conflict sum for how much duration is from current start
                conflict += (duration - (prevDuration)) * currentStart.values.size();
                
                // skip until duration is too small
                if(duration < occurrence.getMinDuration()) {
                    continue;
                }
                
                // set duration to the end of current interval and crop it by max
                // duration
                duration = Math.min(duration, occurrence.getMaxDuration());
                
                // if conflict is smaller than best conflict, store.
                // if duration is larger and conflict is same, store
                if (conflict < bestConflict) {
                    bestConflict = conflict;
                    bestDuration = 0;
                    best.clear();
                }
                if (conflict == bestConflict) {
                    if (duration > bestDuration) {
                        bestDuration = duration;
                        best.clear();
                    }
                    if (duration == bestDuration) {
                        best.add(new OccurrenceAllocation(start, bestDuration));
                    }
                }
                
           }
        }
        
        return best;
    }

}
