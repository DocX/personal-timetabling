/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

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
import net.personaltt.utils.ValuedInterval;

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

   


   
    
    /** Best allocations store class. Provides simplification to storing best
     * of allocations. Allocation a is better than b iff a.cost lt b.cost or (a.duration gt b.duration and a.cost == b.cost)
     */
    private class BestAllocationsStore {
        int bestCost = Integer.MAX_VALUE;
        int bestDuration = 0;
        ArrayList<OccurrenceAllocation> bestAllocations = new ArrayList<>();
        
        boolean storeIfBest(int cost, int duration, int start) {
            if (cost > bestCost) {
                return false;
            }
            
            if (cost < bestCost || duration > bestDuration) {
                bestAllocations.clear();
                bestDuration = duration;
                bestCost = cost;
            }
            
            if (duration < bestDuration) {
                return false;
            }
            
            bestAllocations.add(new OccurrenceAllocation(start, duration));
            return true;
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
   
    /**
     * Walks throught actuall allocations in given interval and search for best possible allocations.
     * @param baseInterval
     * @param occurrence
     * @param schedule
     * @param bestAllocations 
     */
    private void findInDomainInterval(BaseInterval<Integer> baseInterval, Occurrence occurrence, IntervalMultimap<Integer, Occurrence> schedule, BestAllocationsStore bestAllocations) {

        // retrieve list of intervals chungs in given interval
        List<ValuedInterval<Integer, List<Occurrence>>> values = new ArrayList<>();
        for (ValuedInterval<Integer, List<Occurrence>> valuedInterval : schedule.valuesInInterval(baseInterval)) {
            values.add(valuedInterval);
        }
        
        // for all allocations alligned to intervals changes
        for (IntervalsAlignedToStopsIterator it = new IntervalsAlignedToStopsIterator<>((Integer)occurrence.getMinDuration(), values, new IntegerMetric()); it.hasNext();) {
            IntervalsAlignedToStopsIterator<Integer,Integer>.IntervalStop alignment = it.next();
            
            int current = alignment.startValuesIndex;
            int duration = occurrence.getMinDuration();
            
            // if duration is before current interval end, set current to minus one
            // so next stop in cycle below will first select end of first interval, rather
            // than end of next interval, which would skip one stop
            if (values.get(current).getEnd() <= alignment.startPoint + duration) {
                current -= 1;
            }
            
            // for all possible durations, that are aligned to stops
            while (duration <= occurrence.getMaxDuration() && alignment.startPoint + duration <= baseInterval.getEnd()) {
                int cost = computeCostOfAllocation(alignment.startPoint, duration, schedule);
                
                if (bestAllocations.storeIfBest(cost, duration, alignment.startPoint) == false) {
                    // if allocation is not best, more durable allocation will not sure be also best, so can 
                    // break
                    break;
                }
                
                // if duration is maximal, break cycle
                if (duration == occurrence.getMaxDuration()) {
                    break;
                }
                
                // set duration to next stop
                current++;
                if (current == values.size()) {
                    break;
                }
                duration = Math.min(values.get(current).getEnd() - alignment.startPoint, occurrence.getMaxDuration());
            }
           
        }
    }
    
    /**
     * Cost of allocation. Compute cost of given occurrence allocation to given allocations.
     * Cost is sum for each allocation overlapping given allocation of 
     * max(length_over_min_duration - length_of_overlap, 0)/2 + (length_of_overlap - max(length_over_min_duration - length_of_overlap, 0))
     * @param startPoint
     * @param duration
     * @param schedule
     * @return 
     */
    private int computeCostOfAllocation(Integer startPoint, int duration, IntervalMultimap<Integer, Occurrence> schedule) {

        Iterator<Entry<Integer,IntervalMultimap.MultimapEdge<Occurrence>>> edgesIterator = 
                 schedule.edgesIteratorInInterval(new BaseInterval<>(startPoint, startPoint + duration));
        
        int endPoint = startPoint + duration;
        Entry<Integer,IntervalMultimap.MultimapEdge<Occurrence>> currentEdge;
        int cost = 0;
        
        
        // first count allocations that are at the point of startPoint
        if (!edgesIterator.hasNext()) {
            return cost;
        }
        
        currentEdge = edgesIterator.next();
        cost += sumOccurrencesCost(currentEdge.getValue().getValues(), endPoint, startPoint);
        
        // next walk thgrought edges and compute for all new added occurrences
        while(edgesIterator.hasNext()) {
            currentEdge = edgesIterator.next();
            
            cost += sumOccurrencesCost(currentEdge.getValue().getAdded(), endPoint, startPoint);
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
    private int sumOccurrencesCost(List<Occurrence> occurrences, int endPoint, Integer startPoint) {
         int cost = 0;
         for (Occurrence occurrence : occurrences) {
            // for all occurrences that are overlaping start point, count duration
            // that is cropped by its allocation
            
            int durationInOccurrence = Math.min(occurrence.getAllocation().getStart() + occurrence.getAllocation().getDuration(), endPoint) - 
                    Math.max(occurrence.getAllocation().getStart(), startPoint);
            
            // compute duration up to duration that takes over minimal duration as half
            int overMinDuration = Math.max(0, occurrence.getAllocation().getDuration() - occurrence.getMinDuration());
            cost += Math.min(durationInOccurrence, overMinDuration) + Math.max(0, durationInOccurrence - overMinDuration) * 2;
        }
        return cost;
    }
    
    
    
}
