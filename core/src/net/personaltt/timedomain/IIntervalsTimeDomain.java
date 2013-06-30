/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

/**
 *
 * @author docx
 */
public interface IIntervalsTimeDomain {
    
    /**
     * Returns interval set of intervals of the domain which intersects given interval.
     * Implementation must not crop intervals to the given range. So all intervals of the
     * domain that have intersection with given interval nonempty are included in returned
     * intervals set.
     * @param i interval of range in what intervals will be returned
     * @return 
     */
    IntervalsSet getIntervalsIn(Interval i);
    
    /**
     * Determines and return if intervals domain is bounded (has finite number of intervals)
     * or not
     * @return 
     */
    boolean isBounded();
    
    /**
     * Returns bounding interval for intervals domain if is bounded, or null if not
     * @return 
     */
    Interval getBoundingInterval();
    
    /**
     * Determine if two domains intersects. Domains need to be bounded to compute intersects,
     * otherwise if one or both of them are unbounded, implementations must return true
     * @param other
     * @return 
     */
    boolean intersects(IIntervalsTimeDomain other);
}
