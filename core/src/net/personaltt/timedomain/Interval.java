/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import java.util.Objects;
import net.personaltt.utils.BaseInterval;
import org.joda.time.LocalDateTime;

/**
 *
 * @author docx
 */
public class Interval extends BaseInterval<LocalDateTime> implements IIntervalsTimeDomain {

    public Interval(LocalDateTime start, LocalDateTime end) {
        super(start, end);
    }

    @Override
    public String toString() {
        return this.start.toString() + " - " + this.end.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Interval) {
            return this.start.equals(((Interval)obj).start) && this.end.equals(((Interval)obj).end);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.start);
        hash = 83 * hash + Objects.hashCode(this.end);
        return hash;
    }

    public IntervalsSet toIntervalsSet() {
        IntervalsSet s = new IntervalsSet();
        s.unionWith(this);
        return s;
    }
    
    @Override
    public IntervalsSet getIntervalsIn(Interval i) {
        //TODO dumb preobjectized implementation...
        IntervalsSet s = toIntervalsSet();
        s.intersectWith(i.toIntervalsSet());
        return s;
    }
    
    /**
     * Determine if intersection of this with given interval is not empty
     * @param interval
     * @return 
     */
    public boolean intersects(Interval interval ) {
        // this      |  |
        //        | |           false
        //        |   |         true
        //        |      |      true
        //            ||        true
        //            |     |   true
        //                | |   false
        return !(interval.end.isBefore(this.start) || interval.start.isAfter(this.end));
            
    }
    
    /**
     * Determine if given interval is entire in this. Ie intersection of given interval with this 
     * is equal to given interval
     * @param interval
     * @return 
     */
    public boolean overlaps(Interval interval) {
        return 
            (this.start.isBefore(interval.start) || this.start.isEqual(interval.start)) &&
            (this.end.isAfter(interval.end) || this.end.isEqual(interval.end));        

    }

    @Override
    public boolean isBounded() {
        return true;
    }

    @Override
    public Interval getBoundingInterval() {
        return this;
    }

    @Override
    public boolean intersects(IIntervalsTimeDomain other) {
        if (other instanceof Interval) {
            return this.intersects((Interval)other);
        }
        
        return IntervalsTimeDomainUtils.genericIntersects(this, other);
    }
    
}
