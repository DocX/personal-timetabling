package net.personaltt.simplesolver;

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
            
            currentAllocationIntervals.remove(toSolve);
            List<BaseInterval<Integer>> domainWithoutAllocatedSpace = 
                    toSolve.getDomain().getSubtraction(currentAllocationIntervals.intervalsSetIterator()).getBaseIntervals();
            
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
            
            // find largest free interval
            BaseInterval<Integer> largestInterval = findLargestInterval(domainWithoutAllocatedSpace);
            System.out.printf("Found largest free interval: %s\n", largestInterval);
            
            
            if (largestInterval != null) {
                // find fitting duration
                int newDuration = Math.max(
                        toSolve.getMinDuration(), 
                        Math.min(toSolve.getMaxDuration(), largestInterval.getEnd() - largestInterval.getStart())
                        );
                
                int newStart = alignToInterval(toSolve.getDomain(), largestInterval.getStart(), newDuration);
                
                solvingAllocation.setStart(newStart);
                solvingAllocation.setDuration(newDuration);
            }
            
            
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
     * Returns largest interval in given list of intervals
     * @param domain
     * @return 
     */
    private BaseInterval<Integer> findLargestInterval(List<BaseInterval<Integer>> domain) {
        int largestDuration = 0; 
        BaseInterval<Integer> largest = null;
        
        
        for (BaseInterval<Integer> baseInterval : domain) {
            int currentDuration = baseInterval.getEnd() - baseInterval.getStart();
            if (currentDuration > largestDuration )
            {
                largestDuration = currentDuration;
                largest = baseInterval;
            }
        }
        
        return largest;
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
