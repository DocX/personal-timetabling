/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.model;

import net.personaltt.utils.BaseInterval;

/**
 * Occurrence allocation represents allocation of occurrence in time axis.
 * @author docx
 */
public class OccurrenceAllocation {
    
    /**
     * Start of occurrence.
     */
    int start;
    
    /**
     * Duration of occurrence.
     */
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
    
    public int getEnd() {
        return start + duration;
    }

    
    /**
     * Duration of allocation.
     * @return 
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Start of allocation
     * @return 
     */
    public int getStart() {
        return start;
    }
    
     /**
     * Sets values from given allocation
     * @param get 
     */
    public void set(OccurrenceAllocation get) {
        this.start = get.start;
        this.duration = get.duration;
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
        return start + "+" + duration;
    }
 
}
