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
import net.sf.cpsolver.ifs.util.DataProperties;

/**
 *
 * @author docx
 */
public class MainAllocationSelection implements AllocationSelection {

    BestAllocationSelection conflictingSelection;
    
    RouletteAllocationSelection optimizingSelection;
    
    Random random = new Random();
      
    int stayMinConflictSelectionAfterToZeroConflict;
    double probBestIgnoringSelWhenNoConflict;
    
    public MainAllocationSelection(DataProperties properties) {
        
        stayMinConflictSelectionAfterToZeroConflict =
                properties.getPropertyInt("mainAllocationSelection.stayMinConflictSelectionAfterToZeroConflict", 10);
        probBestIgnoringSelWhenNoConflict =
                properties.getPropertyDouble("mainAllocationSelection.probBestIgnoringSelWhenNoConflict", 0.7);
        
        conflictingSelection = new BestAllocationSelection(properties);
        optimizingSelection = new RouletteAllocationSelection(properties);
    }
    
    double conflictCostWeight = 1.0;
    int lastConflictLoweringIteration = 0;
    long lastConflictCost = 0;
    
    // smalest conflict cost statitstic
    long smallestConflict = Long.MAX_VALUE;
    
    @Override
    public OccurrenceAllocation select(SolverState schedule, Occurrence forOccurrence) {
        // todo in last X iterations?
        if (schedule.constraintsCost() < smallestConflict) {
            smallestConflict = schedule.constraintsCost();
        }
        
        // until we are lowering conflict value, store last iteration to current
        if (lastConflictCost > smallestConflict && schedule.constraintsCost() == smallestConflict) {
            lastConflictLoweringIteration = schedule.getItearation();
        }
        lastConflictCost = schedule.constraintsCost();
        
        // if current conflict value is worst than smallest found OR 
        // we are not moving better for last stayMinConflictSelction
        if (schedule.constraintsCost() > smallestConflict || schedule.getItearation() - lastConflictLoweringIteration < stayMinConflictSelectionAfterToZeroConflict) {
            // best minimal conflict
            System.out.printf("AlocSelection: min conflict, min cost. sc:%s, lzi:%s\n",smallestConflict, lastConflictLoweringIteration);
            return conflictingSelection.select(schedule, forOccurrence);
        } else {
            if (random.nextDouble() < probBestIgnoringSelWhenNoConflict) {
                // best conflicting
                System.out.println("AlocSelection: best ignoring conflict");
                return selectBestIgnoringConflict(schedule, forOccurrence);
            } else {
                // roulette selection
                System.out.println("AlocSelection: roullette");
                return optimizingSelection.select(schedule, forOccurrence);
            }
        }
    }
    
    
    public OccurrenceAllocation selectBestIgnoringConflict(SolverState schedule, Occurrence forOccurrence) {
        // Allocations aligned to elementary intervals start and ends
        AlignedOccurrenceAllocationsIterator allocations = 
                new AlignedOccurrenceAllocationsIterator(forOccurrence, schedule.allocationsMultimap());
    
        
        ArrayList<BaseInterval<Integer>> bestAllocations = new ArrayList<>();
        long bestOccurrenceCost = Long.MAX_VALUE;
        
        // iterate thgrouh all aligned allocations
        while(allocations.hasNext()) {
            BaseInterval<Integer> allocation = allocations.next();
            
            long allocationOccurrenceCost = (long)(forOccurrence.getAllocationCost(allocation));
            
            if (allocationOccurrenceCost < bestOccurrenceCost) {
                bestAllocations.clear();
                bestOccurrenceCost = allocationOccurrenceCost;
            }
            
            if (allocationOccurrenceCost == bestOccurrenceCost) {
                bestAllocations.add(allocation);
            }
        }
        
        System.out.printf(" Found %s best conflicting allocations with cost %s: \n",bestAllocations.size(), bestOccurrenceCost);
        
        BaseInterval<Integer> choosen = bestAllocations.get(random.nextInt(bestAllocations.size()));
        return new OccurrenceAllocation(choosen);
    }
    
}
