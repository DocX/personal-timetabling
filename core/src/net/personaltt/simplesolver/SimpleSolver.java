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
import net.personaltt.utils.IntervalMultimap.MultiInterval;

/**
 * Simple solver finds any feasible schedule for personal timetabling problem.
 * @author docx
 */
public class SimpleSolver {

    private Schedule workingSchedule;
    
    long timeoutLimit = 15000;
    
    Random random;
    
    public SimpleSolver() {
        random = new Random();
    }
    
    public SimpleSolver(Random random) {
        this.random = random;
    }
    
    public Schedule solve(ProblemDefinition problem) {
        workingSchedule = (Schedule)problem.initialSchedule.clone();
        
        //TODO domain of all occurrences has to have no interval smaller than
        // minimal duration of occurrence. These domain intervals can be dropped
        
        // Prepare schedule multimap of intervals
        IntervalMultimap<Integer, Occurrence> currentAllocationIntervals = 
                new IntervalMultimap<>();
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : workingSchedule.getOccurrencesAllocations()) {
            currentAllocationIntervals.put(entry.getKey(), entry.getValue().toInterval());
        }
        
        // get conflicting occurrences list
        List<Occurrence> conflictingOccurrences = currentAllocationIntervals.valuesOfOverlappingIntervals();
        
        // Prepare utilities
        long startTime = System.currentTimeMillis();
        
        long iteration = 0;
        
        // while there is conflict, solve it
        while(conflictingOccurrences.size() > 0 && System.currentTimeMillis() - startTime < timeoutLimit) {
            System.out.printf("Iteration %s, %s conflicts\n", iteration, conflictingOccurrences.size());
            
            Occurrence toSolve = conflictingOccurrences.get(random.nextInt(conflictingOccurrences.size()));
            OccurrenceAllocation solvingAllocation = workingSchedule.getAllocationOf(toSolve);
           
            System.out.printf("Selected occurrence %s at %s\n", toSolve, solvingAllocation);
            
            // solve conflict. simple.
            // get domain of occurrence, subtract current schedule without this
            // occurrence from it 
            // and find nearest interval where it fits
            
            
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
            
            // removes solving occurrence from intervals to perform computation on others
            currentAllocationIntervals.remove(toSolve);
            
            // Gets domain of toSolve occurrence splitted to intervals
            // with number of other occurrences allocations in each interval
            List<IntervalMultimap<Integer,Occurrence>.MultiInterval> allocationIntervals = 
                    currentAllocationIntervals.getIntervalsIn(toSolve.getDomain());
            
            
            // then finds start on intervals edges and duration with smallest 
            // conflict value
            List<OccurrenceAllocation> bestAllocations = findLargestAllocationWithSmallestValue(allocationIntervals, toSolve);
            System.out.printf("Found best possible allocations: %s \n", bestAllocations.size());
            
            
            // select random allocation from best list
            solvingAllocation.set(bestAllocations.get(random.nextInt(bestAllocations.size())));
            
            // store in allocation intervals. if it conflicts with existing intervals, returns true
            boolean conflicts = currentAllocationIntervals.put(toSolve,solvingAllocation.toInterval());  
            
            System.out.printf("Resolved as: %s With conflict: %s\n", solvingAllocation, conflicts);
            
            //if (conflicts) {
            //    conflictingOccurrences.add(toSolve);
            //}
            
            conflictingOccurrences = currentAllocationIntervals.valuesOfOverlappingIntervals();
            iteration++;
        }
        
        return workingSchedule;
        
    }
    
    /**
     * Search given multiintervals forlargest feasible allocations with smallest conflict value
     * @param domain
     * @return 
     */
    private List<OccurrenceAllocation> findLargestAllocationWithSmallestValue(List<IntervalMultimap<Integer,Occurrence>.MultiInterval> intervals, Occurrence occurrence) {
        ArrayList<OccurrenceAllocation> best = new ArrayList<>();
        int bestConflict = Integer.MAX_VALUE;
        int bestDuration = 0;
        
        for (int i = 0; i < intervals.size(); i++) {
            int start = intervals.get(i).interval.getStart();
            int conflict = 0;
            
            int j = i;
            // skip smaller intervals
            for (; j < intervals.size(); j++) {

                // if end durration is smaller than min, skip to next
                if (intervals.get(j).interval.getEnd() - start < occurrence.getMinDuration()) {
                    // add conflict
                    conflict += intervals.get(j).values.size() * 
                        (intervals.get(j).interval.getEnd() - intervals.get(j).interval.getStart());
                    
                    continue;
                } else {
                    break;
                }
            }
            
            // in j here is interval ending at first larger duration than minimal duration
            if (j >= intervals.size()) {
                continue;
            }
                       
            // process min duration
            int durationFromStart = occurrence.getMinDuration();
            conflict += intervals.get(j).values.size() * 
                        (start + durationFromStart - intervals.get(j).interval.getStart());
            

            // if conflict is smaller than best conflict, store.
            // if duration is larger and conflict is same, store
            if (conflict < bestConflict) {
                bestConflict = conflict;
                bestDuration = 0;
                best.clear();
            }
            if (conflict == bestConflict) {
                if (durationFromStart > bestDuration) {
                    bestDuration = durationFromStart;
                    best.clear();
                }
                if (durationFromStart == bestDuration) {
                    best.add(new OccurrenceAllocation(start, bestDuration));
                }
            }
            conflict -= intervals.get(j).values.size() * 
                        (start + durationFromStart - intervals.get(j).interval.getStart());
            
            for (; j < intervals.size(); j++) {
                durationFromStart = intervals.get(j).interval.getEnd() - start;
                
                // add conflict for duration from previous duration to current duration
                conflict += intervals.get(j).values.size() * 
                        (intervals.get(j).interval.getEnd() - intervals.get(j).interval.getStart());
                
                if (conflict > bestConflict) {
                    break;
                }
                
                // if conflict is smaller than best conflict, store.
                // if duration is larger and conflict is same, store
                if (conflict < bestConflict) {
                    bestConflict = conflict;
                    bestDuration = 0;
                    best.clear();
                }
                if (conflict == bestConflict) {
                    if (durationFromStart > bestDuration) {
                        bestDuration = durationFromStart;
                        best.clear();
                    }
                    if (durationFromStart == bestDuration) {
                        best.add(new OccurrenceAllocation(start, bestDuration));
                    }
                } 
                
                // if durationFromStart is equal to max, next step is useless
                if (durationFromStart == occurrence.getMaxDuration()) {
                    break;
                }

            }
        }
        
        return best;
    }

    /**
     * Align given interval to itnerval in domain intervalsset. It finds interval where is start of
     * given itnerval and pulls it so it will be entirely inside. This method assumes that in the domain interval
     * containing start is.
     * @param domain
     * @param start
     * @param newDuration
     * @return 
     */
    private int alignToInterval(BaseIntervalsSet<Integer> domain, Integer start, int newDuration) {
        BaseInterval<Integer> domainInterval = domain.getIntervalContaining(start);
        
        System.out.printf("Domain interval containing %s: %s\n", start, domainInterval);

        
        int newStart = start;
        
        // start before domain interval start should never occurre
        // becuase start is from interval constructed from domain minus allocated space
        if (newStart + newDuration > domainInterval.getEnd()) {
            newStart = domainInterval.getEnd() - newDuration;
        }
        
        return newStart;
        
    }
}
