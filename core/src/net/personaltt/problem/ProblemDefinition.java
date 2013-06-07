package net.personaltt.problem;

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
        initialSchedule.allocationMapping.put(occurrence, new OccurrenceAllocation(initialAllocation));
    }
    
}
