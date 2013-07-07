/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import net.personaltt.model.Occurrence;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.BaseInterval;

/**
 * Prioritized conflicting allocation cost.
 * @author docx
 */
public class PriorityConflictAllocationCost extends OccurrenceConflictAllocationCost {

    public PriorityConflictAllocationCost(Occurrence allocatingOccurrence, SolverState solution) {
        super(allocatingOccurrence, solution);
    }

    
    
    @Override
    protected int occurrenceOverMinDurationCost(Occurrence occurrence, BaseInterval<Integer> allocation) {
        if (occurrence.equals(this.allocatingOccurrence)) {
            return 0;
        }
        
        // if it is lesser priority, return zero
        if (occurrence.getPreferredWeight() < this.allocatingOccurrence.getPreferredWeight()) {
            return 0;
        }
        
        return allocation.getEnd() - allocation.getStart();
        
    }
    
}
