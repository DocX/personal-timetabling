package net.personaltt.problem;

import java.util.HashMap;
import java.util.List;

/**
 * Schedule represents allocation of set of occurences
 * @author docx
 */
public class Schedule {
    HashMap<Occurrence, OccurrenceAllocation> allocationMapping;

    private Schedule(int size, Iterable<Occurrence> occurrences) {
        init(size, occurrences);
    }
    
    public Schedule(List<Occurrence> occurrences)  {
        init(occurrences.size(), occurrences);

    }
    
    /**
     * Inits allocation mapping hash with occurrences keys and empty occurrence allocation objects
     * @param size
     * @param occurrences 
     */
    private void init(int size, Iterable<Occurrence> occurrences) {
        this.allocationMapping = new HashMap<>(size);
        
        for (Occurrence occurrence : occurrences) {
             allocationMapping.put(occurrence, new OccurrenceAllocation());
         }
    }

    /**
     * Clones this shedule. Maintains the same keys references to occurrences but
     * creates new OccurrenceAllocation objects for each key with the same value of
     * this
     * @return
     * @throws CloneNotSupportedException 
     */
    @Override
    public Object clone() {
        Schedule clone = new Schedule(this.allocationMapping.size(), this.allocationMapping.keySet());
        
        for (Occurrence occurrence : this.allocationMapping.keySet()) {
            clone.allocationMapping.get(occurrence).copy(this.allocationMapping.get(occurrence));
        }
        
        return clone;
    }
    
    
    
    
}