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
    
    
    
}
