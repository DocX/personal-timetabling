package net.personaltt.problem;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.IntervalMultimap;

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

    public Schedule() {
        this.allocationMapping = new HashMap<>();
    }
    
    public Set<Entry<Occurrence, OccurrenceAllocation>> getOccurrencesAllocations() {
        return Collections.unmodifiableSet(allocationMapping.entrySet());
    }
    
    public OccurrenceAllocation getAllocationOf(Occurrence o){
        return allocationMapping.get(o);
    }
    
    /**
     * Returns interval constructed from occurrence's allocation of given
     * occurrence
     * @param o
     * @return 
     */
    public BaseInterval<Integer> getAllocationIntervalOf(Occurrence o){
        return allocationMapping.get(o).toInterval();
    }
    
    /**
     * Returns occurrence allocation of occurrence with given id. Or null if id 
     * does not exists
     * @param id ID of occurrence
     * @return 
     */
    public OccurrenceAllocation getAllocationOf(int id) {
        return allocationMapping.get(new Occurrence(null, 0, 0, id));
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

    /**
     * Compares given object if is equal schedule. Equal schedule is if
     * has equal occurrences and its allocations
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Schedule) {
            Schedule objSchedule = ((Schedule)obj);
            if (objSchedule.allocationMapping.size() != this.allocationMapping.size()) {
                return false;
            }
            
            for (Entry<Occurrence, OccurrenceAllocation> objEntry : objSchedule.allocationMapping.entrySet()) {
                if (this.allocationMapping.containsKey(objEntry.getKey()) == false) {
                    return false;
                }
                
                if (this.getAllocationOf(objEntry.getKey()).equals(objEntry.getValue()) == false) {
                    return false;
                }
            }
            
            return true;
        }
        
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.allocationMapping);
        return hash;
    }
    
    /**
     * Determine if this schedule has conflicting occurrences allocation
     * @return 
     */
    public boolean hasConflict() {
        IntervalMultimap<Integer, Occurrence> currentAllocationIntervals = 
                new IntervalMultimap<>();
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : getOccurrencesAllocations()) {
            if (currentAllocationIntervals.put(entry.getKey(), entry.getValue().toInterval()) > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    
    
    
}
