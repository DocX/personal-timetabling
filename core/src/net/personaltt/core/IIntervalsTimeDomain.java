/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.core;

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
}
