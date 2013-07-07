/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.personaltt.model.Occurrence;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

/**
 *
 * @author docx
 */
public abstract class OccurrenceConflictAllocationCost implements CostForOccurrence {
    /**
     * Allocating occurrence. It will not be computed to cost, as it is not allocated.
     */
    Occurrence allocatingOccurrence;
    SolverState solution;

    
    
    public OccurrenceConflictAllocationCost() {
    }

    public OccurrenceConflictAllocationCost(Occurrence allocatingOccurrence, SolverState solution) {
        this.allocatingOccurrence = allocatingOccurrence;
        this.solution = solution;
    }

    @Override
    public void setOccurrence(Occurrence occurrence) {
        allocatingOccurrence = occurrence;
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
    @Override
    public long computeCostOfAllocation(BaseInterval<Integer> allocation) {
        int endPoint = allocation.getEnd();
        long cost = 0;
        // get elementary intervals of given allocation
        Iterator<Map.Entry<Integer, IntervalMultimap.MultimapEdge<Occurrence>>> allocationElements = this.solution.allocationsMultimap().edgesIteratorInInterval(allocation);
        // first count allocations that are at the point of startPoint
        Map.Entry<Integer, IntervalMultimap.MultimapEdge<Occurrence>> currentElement = allocationElements.next();
        cost += sumOccurrencesCost(currentElement.getValue().getValues(), allocation);
        // next walk thgrought edges and compute for all new added occurrences
        while (allocationElements.hasNext()) {
            currentElement = allocationElements.next();
            cost += sumOccurrencesCost(currentElement.getValue().getAdded(), allocation);
        }
        return cost;
    }

    protected abstract int occurrenceOverMinDurationCost(Occurrence occurrence, BaseInterval<Integer> allocation);

    /**
     * Sum cost of occurrences over given allocation. It counts half sum for duration that is
     * over minimal duration in occurrence.
     * @param occurrences
     * @param endPoint
     * @param startPoint
     * @return
     */
    protected int sumOccurrencesCost(List<Occurrence> occurrences, BaseInterval<Integer> allocation) {
        int cost = 0;
        for (Occurrence occurrence : occurrences) {
            cost += occurrenceOverMinDurationCost(occurrence, allocation);
        }
        return cost;
    }
    
}
