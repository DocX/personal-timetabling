/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

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
}
