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


   
    private class BestAllocationsStore {
        int bestCost = Integer.MAX_VALUE;
        int bestDuration = 0;
        ArrayList<OccurrenceAllocation> bestAllocations = new ArrayList<>();
        
        void checkAndAdd(int cost, int duration, int start) {
            if (cost > bestCost) {
                return;
            }
            
            if (cost < bestCost || duration < bestDuration) {
                bestAllocations.clear();
                bestDuration = duration;
                bestCost = cost;
            }
            
            if (duration > bestDuration) {
                return;
            }
            
            bestAllocations.add(new OccurrenceAllocation(start, duration));
        }
    }
    
    /**
     * Find all allocations that are smallest in conflict area and then largest in duration
     * in subject of valid allocation for occurrence
     * @param intervals Ordered list of times in which count of allocated occurrences is changes and also definining occurrence domain
     * @param occurrence Occurrence which is subject to finding allocation
     * @return 
     */
    private List<OccurrenceAllocation> findBestAllacations(List<IntervalMultimap<Integer,Occurrence>.MultiIntervalStop> intervals, Occurrence occurrence) {
        // Initialization
        BestAllocationsStore bestAllocations = new BestAllocationsStore();
        
        // index of interval start
        int currentStartInterval = 0;
 
        // process all compact intervals
        while (currentStartInterval < intervals.size()) {
            // returns index of next interval stop after current compact interval end
            currentStartInterval = findInCompactInterval(intervals, currentStartInterval, bestAllocations, occurrence);
        }
        
        
        return bestAllocations.bestAllocations;
    }
    
    /**
     * Finds best allocations in compact interval in intervals starting at currentStartInterval
     * @param intervals
     * @param currentStartInterval
     * @param bestAllocations
     * @param occurrence
     * @return 
     */
    private int findInCompactInterval(List<IntervalMultimap<Integer,Occurrence>.MultiIntervalStop> intervals, int startIntervalIndex, BestAllocationsStore bestAllocations, Occurrence occurrence) {
        
        int start = intervals.get(startIntervalIndex).stop;
        int costSum = 0;
        
        // get interval index, in whish end of allocation lies (end lies in interval i
        // when lower bound of i <= end < upperbound of i)
        int endIntervalIndex = startIntervalIndex;
        
        // when compact intervals shoud at least contain minimal duration +1 index is there always
        while (start + occurrence.getMinDuration() > intervals.get(endIntervalIndex).stop) {
            // add cost of skiped interval
            costSum += getCost(intervals.get(endIntervalIndex)) *
                    (intervals.get(endIntervalIndex).stop - intervals.get(endIntervalIndex+1).stop);    
            
            endIntervalIndex++;
        }
        
        // add cost of portion from start of last interval to the end
        costSum += getCost(intervals.get(endIntervalIndex)) *
                (start + occurrence.getMinDuration() - intervals.get(endIntervalIndex).stop);
          
        // iterate to the end of interval
        int intervalEnd = 0;
        for (int i = endIntervalIndex; i < intervals.size(); i++) {
            intervalEnd = intervals.get(i).stop;
            if (intervals.get(i).isIn == false) {
                break;
            }
        }
        
        // iterate over steps of starts of allocations
        int startNextStop = intervals.get(startIntervalIndex+1).stop;
        int endNextStop = endIntervalIndex+1 < intervals.size() ? intervals.get(endIntervalIndex+1).stop : 0;
        
        while (start + occurrence.getMinDuration() <= intervalEnd) {

            int duration = occurrence.getMinDuration();
            int costSumDuration = costSum;
            int endIntervalIndexDuration = endIntervalIndex;

            while(costSumDuration == costSum) {
                bestAllocations.checkAndAdd(costSumDuration, duration, start);
                
                // add duration to next step
                endIntervalIndexDuration++;
                if (endIntervalIndexDuration >= intervals.size() ||
                    intervals.get(endIntervalIndexDuration-1).isIn==false ||
                    duration == occurrence.getMaxDuration()) {
                    break;
                }                
                int prev_duration = duration;
                duration = intervals.get(endIntervalIndexDuration).stop - start;
                duration = Math.min(duration, occurrence.getMaxDuration());
                
                costSumDuration += getCost(intervals.get(endIntervalIndexDuration-1)) * 
                        (duration - prev_duration);
                
            }
            
            // set next step
            if (endNextStop < startNextStop) {
                break;
            }
            if (startNextStop + occurrence.getMinDuration() <= endNextStop) {
                // alter costSum, remove area from start, add new area at end
                costSum += (startNextStop-start)*(
                        -getCost(intervals.get(startIntervalIndex))
                        +getCost(intervals.get(endIntervalIndex))
                        );
                
                // move start to next stop
                start = startNextStop;
                startIntervalIndex++;
                startNextStop = intervals.get(startIntervalIndex+1).stop;
                
                // move end interval next stop if  now is equal to current end
                if (endNextStop == start + occurrence.getMinDuration()) {
                    endIntervalIndex++;
                    endNextStop = endIntervalIndex+1 < intervals.size() ? intervals.get(endIntervalIndex+1).stop : 0;
                }
            } else {
                costSum += (endNextStop - occurrence.getMinDuration()-start)*(
                        -getCost(intervals.get(startIntervalIndex))
                        +getCost(intervals.get(endIntervalIndex))
                        );
                
                start = endNextStop - occurrence.getMinDuration();
                endIntervalIndex++;
                endNextStop = endIntervalIndex+1 < intervals.size() ? intervals.get(endIntervalIndex+1).stop : 0;
            }
        }
            
        return endIntervalIndex + 1;
    }
    
    int getCost(IntervalMultimap<Integer,Occurrence>.MultiIntervalStop stop) {
        return stop.values == null ? 0 : stop.values.size();
    }
    

}
