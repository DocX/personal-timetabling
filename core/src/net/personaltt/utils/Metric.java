/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

/**
 * Metric interface. Provides simple metric of type M on O type. 
 * Addition, removing metric to and from object and difference of two objects
 * @author docx
 */
public interface Metric<O, M> {
    public O add(O to, M amount);
    public O removes(O from, M amount);
    public M difference(O a, O b);
}
