/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import java.util.Map.Entry;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.IntervalMultimap;

/**
 *
 * @author docx
 */
public class OccurrencesIntervalMultimap extends IntervalMultimap<Integer, Occurrence> {

    /**
     * Initialize IntervalMultimap with occurrences and their allocation intervals in 
     * given schedule
     * @param schedule 
     */
    public OccurrencesIntervalMultimap(Schedule schedule) {
        super();
        
        for (Entry<Occurrence, OccurrenceAllocation> occurrence : schedule.getOccurrencesAllocations()) {
            this.put(occurrence.getKey(), occurrence.getValue().toInterval());
        }
    }
    
}
