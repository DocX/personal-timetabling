/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.solver.core.AllocationSelection;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.RandomUtils;

/**
 * Best allocation selection. Allocation is better if has smaller cost, otherwise if is longer.
 * Selection values allocations on conflicts count basis, thus it can use
 * iteration over aligned allocations to elementary intervals in schedule.
 * 
 * @author docx
 */
public class BestAllocationSelection implements AllocationSelection {

    Random random = new Random();

    @Override
    public OccurrenceAllocation select(SolverState schedule, Occurrence forOccurrence) {
        // Allocations aligned to elementary intervals start and ends
        AlignedOccurrenceAllocationsIterator allocations = 
                new AlignedOccurrenceAllocationsIterator(forOccurrence, schedule.allocationsMultimap());
    
        // cost function. use simpler ConflictSumCost when state is rapidly moving, and 
        // more sofisticated minDurationConflictCost when state is long time tapped in local extrem 
        Cost cost;
        if (tapped(schedule)) {
            cost = new MinDurationConflictAllocationCost(forOccurrence, schedule);
        } else {
            cost = new ConflictSumAllocationCost(forOccurrence, schedule);
        }
        
        ArrayList<BaseInterval<Integer>> bestAllocations = new ArrayList<>();
        int bestDurationInBestCost = 0;
        long bestCost = Long.MAX_VALUE;
        
        // iterate thgrouh all aligned allocations
        while(allocations.hasNext()) {
            BaseInterval<Integer> allocation = allocations.next();
            
            int allocationDuration = (int)(allocation.getEnd() - allocation.getStart());
            
            long allocationCost = cost.computeCostOfAllocation(allocation);
            
            if (allocationCost < bestCost || (allocationCost == bestCost && allocationDuration > bestDurationInBestCost)) {
                bestCost = allocationCost;
                bestAllocations.clear();
                bestDurationInBestCost = allocationDuration;
            }
            
            if (allocationCost == bestCost && allocationDuration == bestDurationInBestCost) {
                bestAllocations.add(allocation);
            }
        }
        
        BaseInterval<Integer> choosen = bestAllocations.get(random.nextInt(bestAllocations.size()));
        return new OccurrenceAllocation(choosen);
    }
    
    private boolean tapped(SolverState schedule) {
        return schedule.getItearation() - schedule.getLastBestIteration() > schedule.allocationsMultimap().size() / 2;
    }

  
    
}
