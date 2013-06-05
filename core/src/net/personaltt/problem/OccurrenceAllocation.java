/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.problem;

/**
 * Occurrence allocation represents allocation of occurrence in time axis.
 * @author docx
 */
class OccurrenceAllocation {
    
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
    
}
