/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.Iterator;
import java.util.Map;
import net.personaltt.model.Occurrence;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.ValuedInterval;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

/**
 * Conflict sum allocation cost. For each occurrence schedulled add
 * cost for length that is overlapped by allocation.
 * 
 * @author docx
 */
public class ConflictSumAllocationCost implements CostForOccurrence {
     /**
     * Allocating occurrence. It will not be computed to cost, as it is not allocated.
     */
    Occurrence allocatingOccurrence;
    
    SolverState solution;

    public ConflictSumAllocationCost(Occurrence allocatingOccurrence, SolverState solution) {
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
    @Override
    public long computeCostOfAllocation(BaseInterval<Integer> allocation) {
        long cost = 0;
        
        // next walk thgrought edges and compute for all new added occurrences
        for (ValuedInterval<Integer, IntervalMultimap.MultimapEdge<Occurrence>> valuedInterval :  this.solution.allocationsMultimap().valuesChangesInInterval(allocation)) {
            
            // add length of element times number of occurrences allocated there.
            cost += valuedInterval.getValues().size() * (valuedInterval.getEnd() - valuedInterval.getStart());
        }
        
        // remove amount countet for occurrence own
        if (allocatingOccurrence != null && allocatingOccurrence.getAllocation() != null) {
            cost -= Math.max(
                0,
                
                Math.min(
                    allocatingOccurrence.getAllocation().getEnd(),
                    allocation.getEnd()
                ) -
                Math.max(
                    allocatingOccurrence.getAllocation().getStart(),
                    allocation.getStart())
                );
        }
        
        return cost;
    }

    @Override
    public void setOccurrence(Occurrence occurrence) {
        allocatingOccurrence = occurrence;
    }
}
