/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.core;

import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.model.Schedule;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

/**
 * Allocation selection selects best allocation for conflicting occurrence
 * @author docx
 */
public interface AllocationSelection {
    OccurrenceAllocation select(SolverState schedule, Occurrence forOccurrence);
}
