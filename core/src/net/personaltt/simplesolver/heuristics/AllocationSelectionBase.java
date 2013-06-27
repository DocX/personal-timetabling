/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import java.util.AbstractMap;
import net.personaltt.utils.IntervalsAlignedToStopsIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.simplesolver.AllocationSelection;
import net.personaltt.simplesolver.IntegerMetric;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.IntervalMultimap;
import net.personaltt.utils.IntervalMultimap.MultimapEdge;
import net.personaltt.utils.IntervalsStopsIterator;
import net.personaltt.utils.Iterables;
import net.personaltt.utils.RandomUtils;
import net.personaltt.utils.ValuedInterval;

/**
 * Simple allocation selection. Selects allocation from occurrence's domain that 
 * has lowest cost. Cost is sum of lenght that allocation shares with other allocations
 * in given shedule for each other allocation.
 * @author docx
 */
public abstract class AllocationSelectionBase implements AllocationSelection {

    Random random = new Random();
    

    
    @Override
    public OccurrenceAllocation select(IntervalMultimap<Integer, Occurrence> schedule, Occurrence forOccurrence) {

        // get all allocations with costs
        ArrayList<AllocationCost> allocations = new ArrayList<>(schedule.size()); 
        valueAllAllocationsInInterval(forOccurrence, schedule, allocations);

        AllocationCost selected = selectFrom(allocations);
        return new OccurrenceAllocation(selected.start, selected.end - selected.start);
    }
    
    abstract protected AllocationCost selectFrom(ArrayList<AllocationCost> allocations);

    
    /**
     * Find all allocations that are smallest in conflict area and then largest in duration
     * in subject of valid allocation for occurrence
     * @param intervals Ordered list of times in which count of allocated occurrences is changes and also definining occurrence domain
     * @param occurrence Occurrence which is subject to finding allocation
     * @return Maximal cost ofd single allocation
     */
    private void valueAllAllocationsInInterval(Occurrence occurrence, IntervalMultimap<Integer, Occurrence> schedule, ArrayList<AllocationCost> allocations) {
        
        
        List<BaseInterval<Integer>> occurrenceDomainIntervals = occurrence.getDomain().getBaseIntervals();
        
        // for all continuous intervals of domain, search possible
        // allocations aligned to points of change values count in schedule
        for (BaseInterval<Integer> baseInterval : occurrenceDomainIntervals) {
            
            // get elementary intervals in continuous domain interval
            ArrayList<ValuedInterval<Integer, MultimapEdge<Occurrence>>> elementaryIntervals = 
                    Iterables.toArrayList(schedule.valuesChangesInInterval(baseInterval));
            
            // enumerator of allocations from minimal to maximal duration, that are aligned to elementary intervals starts
            StopsAlignedAllocationsEnumerator allocationsEnum = 
                    new StopsAlignedAllocationsEnumerator(occurrence.getMinDuration(), occurrence.getMaxDuration(), elementaryIntervals);
            
            
            // foreach allocation. let implementation posible to kill duration walking
            // decision based on previous cost of smaller duration and current cost
            AllocationCost prevAllocation = new AllocationCost();
            prevAllocation.start = -1;
            
            while(allocationsEnum.hasNext()) {
                // compute cost of allocation
                StopsAlignedAllocationsEnumerator.AlignedAllocation allocation = allocationsEnum.next();
                
                int cost = computeCostOfAllocation(allocation, elementaryIntervals);
                
                // store allocation and its cost
                AllocationCost allocationCost = new AllocationCost();
                allocationCost.cost = cost;
                allocationCost.start = allocation.startPoint;
                allocationCost.end = allocation.endPoint;
                
                if (prevAllocation.start == allocationCost.start && killDurationWalk(prevAllocation, allocationCost)) {
                    allocationsEnum.skipToNextStop();
                    continue;
                }
                
                prevAllocation = allocationCost;
                allocations.add(allocationCost);
            }
            
        }
    }
   
    /**
     * Cost of allocation. Compute cost of given occurrence allocation to given allocations.
     * Cost is sum for each allocation overlapping given allocation of 
     * max(length_over_min_duration - length_of_overlap, 0)/2 + (length_of_overlap - max(length_over_min_duration - length_of_overlap, 0))
     * @param startPoint
     * @param duration
     * @param edgesIterator iterator throught edges up to duration
     * @return 
     */
    private int computeCostOfAllocation(StopsAlignedAllocationsEnumerator.AlignedAllocation allocation, ArrayList<ValuedInterval<Integer, MultimapEdge<Occurrence>>> elementaryIntervals) {
        
        int endPoint = allocation.endPoint;
        int currentIndex = allocation.intervalIndexOfStartPoint;
        int cost = 0;
        
        if (!(currentIndex<elementaryIntervals.size() && elementaryIntervals.get(currentIndex).getStart() < endPoint)) {
            return cost;
        }
        
        // first count allocations that are at the point of startPoint
        cost += sumOccurrencesCost(elementaryIntervals.get(currentIndex).getValues().getValues(), allocation);
        
        // next walk thgrought edges and compute for all new added occurrences
        while(currentIndex<elementaryIntervals.size() && elementaryIntervals.get(currentIndex).getStart() < endPoint) {           
            cost += sumOccurrencesCost(elementaryIntervals.get(currentIndex).getValues().getAdded(), allocation);
            currentIndex++;
        }
        
        return cost;
    }
    
    /**
     * Sum cost of occurrences over given allocation. It counts half sum for duration that is 
     * over minimal duration in occurrence.
     * @param occurrences
     * @param endPoint
     * @param startPoint
     * @return 
     */
    private int sumOccurrencesCost(List<Occurrence> occurrences, StopsAlignedAllocationsEnumerator.AlignedAllocation allocation) {
         int cost = 0;
         for (Occurrence occurrence : occurrences) {
            cost += occurrenceCost(occurrence, allocation);
        }
        return cost;
    }
    
     private int occurrenceCost(Occurrence occurrence, StopsAlignedAllocationsEnumerator.AlignedAllocation allocation) {
        // for all occurrences that are overlaping start point, count duration
        // that is cropped by its allocation
        int durationInOccurrence = 
                Math.min(
                    occurrence.getAllocation().getStart() + occurrence.getAllocation().getDuration(), 
                    allocation.endPoint) - 
                Math.max(
                    occurrence.getAllocation().getStart(), 
                    allocation.startPoint);
        
        // compute duration up to duration that takes over minimal duration as half
        int overMinDuration = 
                Math.max(
                    0, 
                    occurrence.getAllocation().getDuration() - occurrence.getMinDuration());
        return 
                Math.min(
                    durationInOccurrence, 
                    overMinDuration) + 
                Math.max(
                    0, 
                    durationInOccurrence - overMinDuration) * 2;
    }

    protected abstract boolean killDurationWalk(AllocationCost prevAllocation, AllocationCost allocationCost) ;
     
     /**
      * Allocation and its cost struct.
      */
     protected class AllocationCost {
         int start;
         int end;
         long cost;
     }

    
    
}
