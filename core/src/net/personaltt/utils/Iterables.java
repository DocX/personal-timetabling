/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author docx
 */
public final class Iterables {
    
    /**
     * Converts iterable to array. Assuming that iterable is not endless.
     * @param <T>
     * @param i
     * @return 
     */
    public static <T> ArrayList<T> toArrayList(Iterable<T> i) {
        ArrayList<T> values = new ArrayList<>();
        for (T val : i) {
            values.add(val);
        }
        
        return values;
    } 
    
}
