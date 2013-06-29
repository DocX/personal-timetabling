package net.personaltt.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

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
        return allocationMapping.entrySet();
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
     * Deep clone of this shedule. Deeply clones schedule - occurrences and its allocation
     * instances. Occurrences are cloned with referencing the same domain object.
     * @return
     * @throws CloneNotSupportedException 
     */
    public Schedule deepClone() {
        Schedule clone = new Schedule();
        
        for (Occurrence occurrence : this.allocationMapping.keySet()) {
            Occurrence occurrenceClone = (Occurrence)occurrence.clone();
            occurrenceClone.allocation = new OccurrenceAllocation(getAllocationOf(occurrence).toInterval());
            
            clone.allocationMapping.put(occurrenceClone, occurrenceClone.allocation);
        }
        
        return clone;
    }
    
    /**
     * Clone of schedule with allocations. References to occurrences instances is
     * kept and occurrences are untouched. Allocations are copied into new schedule map. 
     * @return 
     */
    public Schedule allocationsClone() {
        Schedule clone = new Schedule();
        
        for (Occurrence occurrence : this.allocationMapping.keySet()) {
            clone.allocationMapping.put(occurrence, new OccurrenceAllocation(getAllocationOf(occurrence).toInterval()));
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
        IntervalMultimap<Occurrence> currentAllocationIntervals = 
                new IntervalMultimap<>();
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : getOccurrencesAllocations()) {
            if (currentAllocationIntervals.put(entry.getKey(), entry.getValue().toInterval(), null) > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    
    public int numberOccurrences() {
        return this.allocationMapping.size();
    }

    public OccurrenceAllocation removeAllocation(Occurrence toSolve) {
        return this.allocationMapping.remove(toSolve);
    }

    public void setAllocation(Occurrence toSolve, OccurrenceAllocation solvingAllocation) {
        this.allocationMapping.put(toSolve, solvingAllocation);
    }
    
}
