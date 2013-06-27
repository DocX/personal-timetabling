/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import java.util.ArrayList;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.utils.RandomUtils;

/**
 * Best allocation selection. Allocation is better if has smaller cost, otherwise if is longer.
 * 
 * @author docx
 */
public class BestAllocationSelection extends AllocationSelectionBase {

    @Override
    protected AllocationCost selectFrom(ArrayList<AllocationCost> allocations) {
        
        ArrayList<AllocationCost> bestAllocations = new ArrayList<>(allocations.size()/2);
        int bestDurationInBestCost = 0;
        long bestCost = Long.MAX_VALUE;
        
        for (AllocationCost allocationCost : allocations) {
            int allocationDuration = (int)(allocationCost.end - allocationCost.start);
            if (allocationCost.cost < bestCost || (allocationCost.cost == bestCost && allocationDuration > bestDurationInBestCost)) {
                bestCost = allocationCost.cost;
                bestAllocations.clear();
                bestDurationInBestCost = allocationDuration;
            }
            
            if (allocationCost.cost == bestCost && allocationDuration == bestDurationInBestCost) {
                bestAllocations.add(allocationCost);
            }
        }
        
        return bestAllocations.get(random.nextInt(bestAllocations.size()));
    }

    @Override
    protected boolean killDurationWalk(AllocationCost prevAllocation, AllocationCost allocationCost) {
        // if cost is greater than previous, no sense of walking to longer duration
        
        return prevAllocation.cost < allocationCost.cost;
    }
    
    
    
}
