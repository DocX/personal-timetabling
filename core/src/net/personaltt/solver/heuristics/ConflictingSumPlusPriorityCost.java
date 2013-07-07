/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import net.personaltt.model.Occurrence;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.BaseInterval;

/**
 *
 * @author docx
 */
public class ConflictingSumPlusPriorityCost extends ConflictSumAllocationCost {

    public ConflictingSumPlusPriorityCost(Occurrence allocatingOccurrence, SolverState solution) {
        super(allocatingOccurrence, solution);
    }

    
   
    public long computeCostOfAllocation(BaseInterval<Integer> allocation) {
        long cost = super.computeCostOfAllocation(allocation);
        
        cost += Math.abs(this.allocatingOccurrence.getPreferredStart() - allocation.getStart()) * 
                (this.allocatingOccurrence.getPreferredWeight() - 1);
        
        return cost;
    }
    
}
