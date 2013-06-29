/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.solver.core.AllocationSelection;
import net.personaltt.solver.core.SolverState;

/**
 *
 * @author docx
 */
public class MainAllocationSelection implements AllocationSelection {

    BestAllocationSelection conflictingSelection;
    
    RouletteAllocationSelection optimizingSelection;
    
    double conflictCostWeightAccellerator = 1.1;
    
    public MainAllocationSelection() {
        conflictingSelection = new BestAllocationSelection();
        optimizingSelection = new RouletteAllocationSelection();
    }
    
    double conflictCostWeight = 1.0;
    
    @Override
    public OccurrenceAllocation select(SolverState schedule, Occurrence forOccurrence) {
        
        if (schedule.constraintsCost() > 0) {
            conflictCostWeight = Math.min(1.0, conflictCostWeight * conflictCostWeightAccellerator);
            conflictingSelection.conflictCostWeight = conflictCostWeight;
            return conflictingSelection.select(schedule, forOccurrence);
        } else {
            conflictCostWeight = 0.0001;
            return optimizingSelection.select(schedule, forOccurrence);
        }
    }
    
}
