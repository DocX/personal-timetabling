/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.personaltt.model.Occurrence;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.ValuedInterval;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

/**
 * Minimal duration conflict cost of allocation.
 * Cost is sum of overlapping lengths of other occurrences which are weighted by
 * if it is length above minimal duration. Lengths that fits into over minimal
 * duration are halved in cost.
 * @author docx
 */
public class MinDurationConflictAllocationCost {
    
    /**
     * Allocating occurrence. It will not be computed to cost, as it is not allocated.
     */
    Occurrence allocatingOccurrence;
    
    SolverState solution;

    public MinDurationConflictAllocationCost(Occurrence allocatingOccurrence, SolverState solution) {
        this.allocatingOccurrence = allocatingOccurrence;
        this.solution = solution;
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
    public int computeCostOfAllocation(BaseInterval<Integer> allocation) {
        
        int endPoint = allocation.getEnd();
        int cost = 0;
        
        // get elementary intervals of given allocation
        Iterator<Map.Entry<Integer,IntervalMultimap.MultimapEdge<Occurrence>>> allocationElements =
                this.solution.allocationsMultimap().edgesIteratorInInterval(allocation);
        
        // first count allocations that are at the point of startPoint
        Map.Entry<Integer,IntervalMultimap.MultimapEdge<Occurrence>> currentElement = allocationElements.next();
        cost += sumOccurrencesCost(currentElement.getValue().getValues(), allocation);
        
        // next walk thgrought edges and compute for all new added occurrences
        while(allocationElements.hasNext()) {
            currentElement = allocationElements.next();
            
            cost +=  sumOccurrencesCost(currentElement.getValue().getAdded(), allocation);
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
    private int sumOccurrencesCost(List<Occurrence> occurrences, BaseInterval<Integer> allocation) {
         int cost = 0;
         for (Occurrence occurrence : occurrences) {
            cost += occurrenceCost(occurrence, allocation);
        }
        return cost;
    }
    
     private int occurrenceCost(Occurrence occurrence, BaseInterval<Integer> allocation) {
        if (occurrence.equals(this.allocatingOccurrence)) {
            return 0;
        }
         
        // for all occurrences that are overlaping start point, count duration
        // that is cropped by its allocation
        int durationInOccurrence = 
                Math.min(
                    occurrence.getAllocation().getStart() + occurrence.getAllocation().getDuration(), 
                    allocation.getEnd()) - 
                Math.max(
                    occurrence.getAllocation().getStart(), 
                    allocation.getStart());
        
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
}
