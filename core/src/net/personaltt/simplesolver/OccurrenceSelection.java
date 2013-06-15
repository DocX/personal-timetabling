/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import java.util.Map;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.IntervalMultimap;

/**
 * Occurrence selection implements selection of occurrence to change in each iteration
 * @author docx
 */
public interface OccurrenceSelection {
    Occurrence select(IntervalMultimap<Integer, Occurrence> currentSchedule);
}
