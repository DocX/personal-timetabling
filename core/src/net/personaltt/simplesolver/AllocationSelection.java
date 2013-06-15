/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.IntervalMultimap;

/**
 * Allocation selection selects best allocation for conflicting occurrence
 * @author docx
 */
public interface AllocationSelection {
    OccurrenceAllocation select(IntervalMultimap<Integer, Occurrence> schedule, Occurrence forOccurrence);
}
