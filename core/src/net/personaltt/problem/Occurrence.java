package net.personaltt.problem;

import net.personaltt.utils.BaseIntervalsSet;

/**
 * Occurrence represents definition of one time quantum in the time axis. It means its 
 * time domain and ID. Current allocation will be held separately
 * @author docx
 */
public class Occurrence {
    
    /**
     * Intervals set representing occurrence's domain of what area on time axis it
     * can cover
     */
    BaseIntervalsSet<Integer> domain;
    
    /*
     * Lower bound of duration interval
     */
    int duration_min;
    
    /**
     * Upper bound of duration interval
     */
    int duration_max;
    
    /**
     * ID of occurrence used to associate with other parts of algorithm and client
     */
    int id;

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            return this.id == ((Occurrence)(obj)).id;
        }
        
        return super.equals(obj);
    }
    
    
    
}