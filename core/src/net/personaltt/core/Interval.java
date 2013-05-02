/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.core;

import java.util.Objects;
import org.joda.time.LocalDateTime;

/**
 *
 * @author docx
 */
public class Interval implements IIntervalsTimeDomain {
    LocalDateTime start;
    LocalDateTime end;

    public Interval(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public LocalDateTime getStart() {
        return start;
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
    
    
    
    
}
