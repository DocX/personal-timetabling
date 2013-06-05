/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

/**
 * Interval interface for generic type T.
 * @author docx
 */
public class BaseInterval<T> {
    
    protected T start;
    protected T end;

    public BaseInterval(T start, T end) {
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
