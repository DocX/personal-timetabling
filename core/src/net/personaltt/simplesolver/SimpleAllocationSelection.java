/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import net.personaltt.utils.IntervalsAlignedToStopsIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.IntervalMultimap;
import net.personaltt.utils.IntervalMultimap.ValuesInterval;

/**
 * Simple allocation selection. Selects allocation from occurrence's domain that 
 * has lowest cost. Cost is sum of lenght that allocation shares with other allocations
 * in given shedule for each other allocation.
 * @author docx
 */
public class SimpleAllocationSelection implements AllocationSelection {

    Random random = new Random();
    
    @Override
    public OccurrenceAllocation select(IntervalMultimap<Integer, Occurrence> schedule, Occurrence forOccurrence) {

        // find all allocations in domain of toSolve, which are
        // minimal in area of conflict by allocationInterals
        // and then maximal in duration
        List<OccurrenceAllocation> bestAllocations = findBestAllacations(forOccurrence, schedule);
        
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
    private List<OccurrenceAllocation> findBestAllacations(Occurrence occurrence, IntervalMultimap<Integer, Occurrence> schedule) {
        // Initialization
        BestAllocationsStore bestAllocations = new BestAllocationsStore();
        
        List<BaseInterval<Integer>> occurrenceDomainIntervals = occurrence.getDomain().getBaseIntervals();
        
        // for all continuous intervals of domain, search possible
        // allocations aligned to points of change values count in schedule
        for (BaseInterval<Integer> baseInterval : occurrenceDomainIntervals) {
            findInDomainInterval(baseInterval, occurrence, schedule, bestAllocations);
        }
        
        return bestAllocations.bestAllocations;
    }
   
    private void findInDomainInterval(BaseInterval<Integer> baseInterval, Occurrence occurrence, IntervalMultimap<Integer, Occurrence> schedule, BestAllocationsStore bestAllocations) {

        List<IntervalMultimap<Integer, Occurrence>.ValuesInterval> values = 
                schedule.valuesInInterval(baseInterval);
        
        for (Iterator<IntervalsAlignedToStopsIterator<Integer,Integer>.IntervalStop> it = new IntervalsAlignedToStopsIterator<>((Integer)occurrence.getMinDuration(), values, new IntegerMetric()); it.hasNext();) {
            IntervalsAlignedToStopsIterator<Integer,Integer>.IntervalStop stop = it.next();
            
            // compute cost between startPoint and endPoint
            SimpleCostCoutner cost = new SimpleCostCoutner();
            
            // compute from start to interval before end interval
            // so intervals that are full to end of interval
            int current = stop.startValuesIndex;
            int endPoint = stop.startPoint + occurrence.getMinDuration();
            
            while(current < values.size() && values.get(current).getStart() < endPoint) {
                
                // length of current interval cropped by start and end point
                int length = 
                        Math.min(endPoint, values.get(current).getEnd()) -
                        Math.max(stop.startPoint, values.get(current).getStart());
                
                cost.add(length, values.get(current).getValues());
                
                current++;
            }
            
            // here in cost is cost of allocation of minimal duration
            bestAllocations.checkAndAdd(cost.cost, endPoint - stop.startPoint, stop.startPoint);
            
            // current is now on the first interval after minimal duration
            // if minimal duration ends inside last interval, revert to it
            if (current < values.size() && values.get(current).getStart() > endPoint) {
                current--;
            }
            
            // walk by next intervals stops to enlarge duration
            while(current < values.size() && values.get(current).getStart() <
                    stop.startPoint + occurrence.getMaxDuration() && 
                    endPoint < stop.startPoint + occurrence.getMaxDuration()) {
                
                int beforeEndPoint = endPoint;
                endPoint = Math.min(
                        stop.startPoint + occurrence.getMaxDuration(), 
                        values.get(current).getEnd());
                 
                
                cost.add(endPoint - beforeEndPoint, values.get(current).getValues());
                bestAllocations.checkAndAdd(cost.cost, endPoint - stop.startPoint, stop.startPoint);
                current++;
            }
        }
    }
    
    /**
     * Simple cost counter. Counts sum of lengths of occurrences
     */
    private class SimpleCostCoutner {
        int cost = 0;
        
        void add(int length, List<Occurrence> occurrences) {
            cost += length * occurrences.size();
        }
    }
    
    
}
