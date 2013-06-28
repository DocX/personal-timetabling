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

    AllocationSelection conflictingSelection;
    
    AllocationSelection optimizingSelection;
    
    public MainAllocationSelection() {
        conflictingSelection = new BestAllocationSelection();
        optimizingSelection = new RouletteAllocationSelection();
    }
    
    @Override
    public OccurrenceAllocation select(SolverState schedule, Occurrence forOccurrence) {
        
        if (schedule.constraintsCost() > 0) {
            return conflictingSelection.select(schedule, forOccurrence);
        } else {
            return optimizingSelection.select(schedule, forOccurrence);
        }
        
    }
    
}
