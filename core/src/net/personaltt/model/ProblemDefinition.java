package net.personaltt.model;

import java.util.ArrayList;
import java.util.List;
import net.personaltt.utils.BaseInterval;

/**
 * Problem definition encapsulates all parameters for solver. It consists of
 * - Set of time occurences to be object of solver
 * - Initial allocation of these occurences
 * - Time domains
 * - Priority relation between occurences
 * @author docx
 */
public class ProblemDefinition {
    
    public List<Occurrence> problemOccurrences;
    
    public Schedule initialSchedule;

    public ProblemDefinition() {
        problemOccurrences = new ArrayList<>();
        initialSchedule = new Schedule();
    }
    
    
    /**
     * Adds occurrence to problem definition with initial allocation
     * @param occurrence
     * @param initialAllocation 
     */
    public void addOccurrence(Occurrence occurrence, BaseInterval<Integer> initialAllocation) {
        problemOccurrences.add(occurrence);
        OccurrenceAllocation allocationInstance = new OccurrenceAllocation(initialAllocation);
        occurrence.setInitialAllocation(allocationInstance);;
        initialSchedule.allocationMapping.put(occurrence, allocationInstance);
    }

    public void removeOccurrence(Occurrence occurrence) {
        problemOccurrences.remove(occurrence);
        initialSchedule.removeAllocation(occurrence);
    }
    
}
