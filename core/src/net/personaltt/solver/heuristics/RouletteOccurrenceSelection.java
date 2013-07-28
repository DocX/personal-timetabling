/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.personaltt.model.Occurrence;
import net.personaltt.model.Schedule;
import net.personaltt.solver.core.OccurrenceSelection;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;
import net.personaltt.utils.RandomUtils;
import net.personaltt.utils.ValuedInterval;
import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * Roulette occurrence selection. Selects occurrence with larger cost with more probability
 * cost is sum of lengths shared with occurrence for each of other occurrences allocations
 * @author docx
 */
public class RouletteOccurrenceSelection implements OccurrenceSelection {

    Random random = new Random();   
    
    double optimizationWhenConflictProb;

    public RouletteOccurrenceSelection(DataProperties properties) {
        // EXPERIMENT with added probability of also counting preference cost when
        // conflict solving phase. According to experiments, zero is best so far.
        optimizationWhenConflictProb = 
                properties.getPropertyDouble("rouletteOccurrenceSelection.optimizationWhenConflictProb", 0);
    }
    
     /**
     * Gets occurrence to solve using heuristic from conflicting occurrences with
     * cost
     * @param conflictingOccurrencesWithCost
     * @return ConflictSumAllocationCost
     */    
    @Override
    public Occurrence select(SolverState solution) {
        // select random unassigned
        if (solution.getUnassignedOccurrences().size() > 0) {
            return solution.getUnassignedOccurrences().get(random.nextInt(solution.getUnassignedOccurrences().size()));
        }
        
        // select more problematic occurrence with more probability
        //TODO original ConflictSumCost
        CostForOccurrence conflicting = new ConflictSumAllocationCost(null, solution);
        long totalConflictCost = 0;
        long totalAllocationCost = 0;
        
        long[] occurrenceConflictCost = new long[solution.allocationsMultimap().size()];
        int i = 0;
        for (Occurrence occurrence : solution.allocationsMultimap().keys()) {
            conflicting.setOccurrence(occurrence);
            occurrenceConflictCost[i] = conflicting.computeCostOfAllocation(occurrence.getAllocation().toInterval());
            
            totalConflictCost += occurrenceConflictCost[i] ;
            totalAllocationCost += occurrence.getPreferrenceCost();
            i++;
        }
        
        if (totalAllocationCost == 0 && totalConflictCost == 0) {
            return null;
        }
        
        boolean countAllocationCost = totalConflictCost == 0;
        if (totalConflictCost > 0 && random.nextDouble() < optimizationWhenConflictProb) {
            countAllocationCost = true;
        }
        
        
        
        // get random in sum
        long totalCost = totalConflictCost + (countAllocationCost ? totalAllocationCost : 0);
        long selection = RandomUtils.nextLong(random, totalCost);
        
        /*
        System.out.printf("Selection total conflict: %s, cost: %s, count cost: %s, selected: %s\n", 
                totalConflictCost,
                totalAllocationCost,
                countAllocationCost,
                selection
                );
        */
        
        // go throught occurrences and first where sum is less than random
        totalCost = 0;
        i = 0;
        for (Occurrence occurrence : solution.allocationsMultimap().keys()) {
            long cost = occurrenceConflictCost[i] + (countAllocationCost ? occurrence.getPreferrenceCost() : 0);
            i++;
            if (cost == 0) {
                continue;
            }
            totalCost += cost;
            if (totalCost > selection) {
                return occurrence;
            }
            
        }
        throw new IllegalStateException();
    }
    
    
}
