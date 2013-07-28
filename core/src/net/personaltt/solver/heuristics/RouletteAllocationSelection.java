/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.ArrayList;
import java.util.Random;
import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.solver.core.AllocationSelection;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.RandomUtils;
import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * Roulette allocations selection. Select allocation from
 * all allocations that have locally extremal cost (cost is conflict number)
 * so that probability of selecting allocation a is (sum of costs)/(cost of a).
 * @author docx
 */
public class RouletteAllocationSelection implements AllocationSelection {

    Random random = new Random();

    public RouletteAllocationSelection(DataProperties properties) {
    }
    
    
    
    @Override
    public OccurrenceAllocation select(SolverState schedule, Occurrence forOccurrence) {
        // Allocations aligned to elementary intervals start and ends
        AlignedOccurrenceAllocationsIterator allocations = 
                new AlignedOccurrenceAllocationsIterator(forOccurrence, schedule.allocationsMultimap());
    
        // cost function
        //MinDurationConflictAllocationCost cost = 
        //        new MinDurationConflictAllocationCost(forOccurrence, schedule);
        
        // save allocations to list
        ArrayList<AllocationAndCost> allocationsList = new ArrayList<>();
        
        while(allocations.hasNext()) {
            BaseInterval<Integer> allocation = allocations.next();
            if (allocation.equals(forOccurrence.getAllocation().toInterval())) {
                continue;
            }
            
            long allocationCost = forOccurrence.getPreferrenceCost(allocation);
            
            allocationsList.add(new AllocationAndCost(allocation, 1d/(allocationCost+1)));
        }
        
        // compute sum
        double valueSum = 0;
        for (int i = 0; i < allocationsList.size(); i++) {
            valueSum += allocationsList.get(i).cost;
        }
        
        // select 
        double selection = random.nextDouble() * valueSum;
        
        // retrieve selected
        valueSum = 0;
        for (int i = 0; i < allocationsList.size(); i++) {
            valueSum += allocationsList.get(i).cost;
            if (valueSum > selection) {
                
                return new OccurrenceAllocation(allocationsList.get(i).allocation);
            }
        }
        
        throw new IllegalStateException();
    }
    
    private class AllocationAndCost {
        BaseInterval<Integer> allocation;
        double cost;

        public AllocationAndCost(BaseInterval<Integer> allocation, double cost) {
            this.allocation = allocation;
            this.cost = cost;
        }
    }
    
    
}
