/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import java.util.ArrayList;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.utils.RandomUtils;

/**
 *
 * @author docx
 */
public class RouletteAllocationSelection extends AllocationSelectionBase {

     /**
     * value exponent. Each occurrence cost is powered to value exponent,
     * in roulette selection. Positive numbers will make greater values even 
     * greater probability.
     */
    double valueExp = 1;
    
    @Override
    protected AllocationCost selectFrom(ArrayList<AllocationCost> allocations) {
        // get max cost
        long maxCost = 0;
        for (AllocationSelectionBase.AllocationCost allocationCost : allocations) {
            if (allocationCost.cost > maxCost) { maxCost = allocationCost.cost; }
        }
        
        // compute inversion of cost
        double valueSum = 0;
        for (AllocationSelectionBase.AllocationCost allocationCost : allocations) {
            valueSum += Math.pow(maxCost - allocationCost.cost, valueExp);
        }
        
        // select 
        long selection = RandomUtils.nextLong(random, (long)Math.ceil(valueSum));
        
        // retrieve selected
        valueSum = 0;
        for (AllocationSelectionBase.AllocationCost allocationCost : allocations) {
            valueSum += Math.pow(maxCost - allocationCost.cost, valueExp);
            if (valueSum > selection) {
                return allocationCost;
            }
        }
        
        throw new IllegalStateException();
    }

    @Override
    protected boolean killDurationWalk(AllocationCost prevAllocation, AllocationCost allocationCost) {
        return false;
    }
    
    
}
