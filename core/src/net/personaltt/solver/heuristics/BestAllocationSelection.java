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
import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * Best allocation selection. Allocation is better if has smaller cost, otherwise if is longer.
 * Selection values allocations on conflicts count basis, thus it can use
 * iteration over aligned allocations to elementary intervals in schedule.
 * 
 * @author docx
 */
public class BestAllocationSelection implements AllocationSelection {

    Random random = new Random();
    
    double conflictCostWeight;
    double valueCostWeight;

    public BestAllocationSelection(DataProperties properties) {
        conflictCostWeight = properties.getPropertyDouble("bestAllocationSelection.conflictCostWeight", 1.0);
        valueCostWeight = properties.getPropertyDouble("bestAllocationSelection.valueCostWeight", 1.0);
    }

    
    @Override
    public OccurrenceAllocation select(SolverState schedule, Occurrence forOccurrence) {
        // Allocations aligned to elementary intervals start and ends
        AlignedOccurrenceAllocationsIterator allocations = 
                new AlignedOccurrenceAllocationsIterator(forOccurrence, schedule.allocationsMultimap());
    
        // cost function. use simpler ConflictSumCost when state is rapidly moving, and 
        // more sofisticated minDurationConflictCost when state is long time tapped in local extrem 
        Cost cost;
        //if (schedule.constraintsCost() > 0) {
            cost = new MinDurationConflictAllocationCost(forOccurrence, schedule);
        //} else {
        //    cost = new ConflictSumAllocationCost(forOccurrence, schedule);
        //}
        
        ArrayList<BaseInterval<Integer>> bestAllocations = new ArrayList<>();
        long bestOccurrenceCost = Long.MAX_VALUE;
        long bestConflictCost = Long.MAX_VALUE;
        
        BaseInterval<Integer> occurrenceAllocation = forOccurrence.getAllocation().toInterval();
        
        // iterate thgrouh all aligned allocations
        while(allocations.hasNext()) {
            BaseInterval<Integer> allocation = allocations.next();
            
            // skip same allocation
            if (allocation.equals(occurrenceAllocation)) {
                continue;
            }
            
            long allocationOccurrenceCost = valueCostWeight == 1.0 ? forOccurrence.getPreferrenceCost(allocation) : (long)(forOccurrence.getPreferrenceCost(allocation) * valueCostWeight);
            
            long allocationConflictCost =
                    conflictCostWeight == 1.0 ?
                    cost.computeCostOfAllocation(allocation) :
                    (long)(cost.computeCostOfAllocation(allocation) * conflictCostWeight);
            
            //System.out.printf(" %s c%s:%s\n", allocation, allocationConflictCost, allocationOccurrenceCost);
            
            if (allocationConflictCost < bestConflictCost || (allocationConflictCost == bestConflictCost && allocationOccurrenceCost < bestOccurrenceCost)) {
                bestConflictCost = allocationConflictCost;
                bestAllocations.clear();
                bestOccurrenceCost = allocationOccurrenceCost;
            }
            
            if (allocationConflictCost == bestConflictCost && allocationOccurrenceCost == bestOccurrenceCost) {
                bestAllocations.add(allocation);
            }
        }
        
        //System.out.printf("Found best %s:%s allocations: %s\n", bestConflictCost, bestOccurrenceCost, bestAllocations.size());
        if (bestAllocations.isEmpty()) {
            return null;
        }
        
        BaseInterval<Integer> choosen = bestAllocations.get(random.nextInt(bestAllocations.size()));
        return new OccurrenceAllocation(choosen);
    }
    
  

  
    
}
