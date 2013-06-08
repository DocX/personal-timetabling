/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.problem;

import net.personaltt.utils.BaseInterval;

/**
 * Occurrence allocation represents allocation of occurrence in time axis.
 * @author docx
 */
public class OccurrenceAllocation {
    
    int start;
    
    int duration; 

    public OccurrenceAllocation() {
        this.start = 0;
        this.duration = 0;
    }
    
    public OccurrenceAllocation(int start, int duration) {
        this.start = start;
        this.duration = duration;
    }
    
    public OccurrenceAllocation(BaseInterval<Integer> allocation) {
        this.start = allocation.getStart();
        this.duration = allocation.getEnd() - this.start;
    }
    
    /**
     * Sets this values to values of other
     * @param other 
     */
    public void copy(OccurrenceAllocation other) {
        this.start = other.start;
        this.duration = other.duration;
    }
    
    /**
     * Returns interval of this allocation
     * @return 
     */
    public BaseInterval<Integer> toInterval() {
        return new BaseInterval<>(start, start + duration);
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public int getStart() {
        return start;
    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OccurrenceAllocation) {
            return ((OccurrenceAllocation)obj).duration == this.duration 
                    && ((OccurrenceAllocation)obj).start == this.start; 
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + this.start;
        hash = 31 * hash + this.duration;
        return hash;
    }

    @Override
    public String toString() {
        return String.format("%s+%s", start, duration);
    }

    public void set(OccurrenceAllocation get) {
        this.start = get.start;
        this.duration = get.duration;
    }
    
    
    
    
    
    
    
}
