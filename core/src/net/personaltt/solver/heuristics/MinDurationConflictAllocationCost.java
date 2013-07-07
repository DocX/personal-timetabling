/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.personaltt.model.Occurrence;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.ValuedInterval;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

/**
 * Minimal duration conflict cost of allocation.
 * Cost is sum of overlapping lengths of other occurrences which are weighted by
 * if it is length above minimal duration. Lengths that fits into over minimal
 * duration are halved in cost.
 * @author docx
 */
public class MinDurationConflictAllocationCost extends OccurrenceConflictAllocationCost {


    
    public MinDurationConflictAllocationCost(Occurrence allocatingOccurrence, SolverState solution)  {
        this.allocatingOccurrence = allocatingOccurrence;
        this.solution = solution;
    }
    
    @Override
    public int occurrenceOverMinDurationCost(Occurrence occurrence, BaseInterval<Integer> allocation) {
        if (occurrence.equals(this.allocatingOccurrence)) {
            return 0;
        }
         
        // for all occurrences that are overlaping start point, count duration
        // that is cropped by its allocation
        int durationInOccurrence = 
                Math.min(
                    occurrence.getAllocation().getStart() + occurrence.getAllocation().getDuration(), 
                    allocation.getEnd()) - 
                Math.max(
                    occurrence.getAllocation().getStart(), 
                    allocation.getStart());
        
        // compute duration up to duration that takes over minimal duration as half
        int overMinDuration = 
                Math.max(
                    0, 
                    occurrence.getAllocation().getDuration() - occurrence.getMinDuration());
        
        return  
                Math.min(
                    durationInOccurrence, 
                    overMinDuration) + 
                Math.max(
                    0, 
                    durationInOccurrence - overMinDuration) * 2;
        
        
    }
     
     
}
