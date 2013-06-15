/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.utils.IntervalMultimap;

/**
 *
 * @author docx
 */
public class SimpleAllocationSelection implements AllocationSelection {

    Random random = new Random();
    
    @Override
    public OccurrenceAllocation select(IntervalMultimap<Integer, Occurrence> schedule, Occurrence forOccurrence) {
        // allocationIntervals = ordered list of triples t_i time, n_i N, d_i bool, 
            // so that in interval t_i - t_i+1 there is n_i allocated occurences in 
            // currentAllocationIntervals and union of all t_i - t_i+1 where d_i = true
            // is equal to domain of toSolve.
            List<IntervalMultimap<Integer,Occurrence>.MultiIntervalStop> allocationIntervals = 
                    schedule.getIntervalsIn(forOccurrence.getDomain());
            
            
            // find all allocations in domain of toSolve, which are
            // minimal in area of conflict by allocationInterals
            // and then maximal in duration
            List<OccurrenceAllocation> bestAllocations = findBestAllacations(
                    allocationIntervals, 
                    forOccurrence
                    );
            System.out.printf("No. of found best possible allocations: %s \n", bestAllocations.size());
            
            return bestAllocations.get(random.nextInt(bestAllocations.size()));
    }
    
     private class BestAllocationsStore {
        int bestCost = Integer.MAX_VALUE;
        int bestDuration = 0;
        ArrayList<OccurrenceAllocation> bestAllocations = new ArrayList<>();
        
        void checkAndAdd(int cost, int duration, int start) {
            if (cost > bestCost) {
                return;
            }
            
            if (cost < bestCost || duration > bestDuration) {
                bestAllocations.clear();
                bestDuration = duration;
                bestCost = cost;
            }
            
            if (duration < bestDuration) {
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
