/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.Objects;

/**
 * Interval interface for generic type T.
 * @author docx
 */
public class BaseInterval<T extends Comparable> {
    
    protected T start;
    protected T end;

    public BaseInterval(T start, T end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("Start of interval must be before its end");
        }
        
        this.start = start;
        this.end = end;
    }
    
    public T getStart() {
        return this.start;
    }
    
    public T getEnd() {
        return this.end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseInterval) {
            return ((BaseInterval)obj).start.equals(this.start) && ((BaseInterval)obj).end.equals(this.end);
        }
        
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.start);
        hash = 47 * hash + Objects.hashCode(this.end);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("Interval %s - %s", this.start, this.end);
    }
    
}
