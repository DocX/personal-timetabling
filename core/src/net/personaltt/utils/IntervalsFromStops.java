/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Intervals from stops. Wrapper around stops iterator of intervals providing
 * iterations over intervals between stops.
 * @author docx
 */
public class IntervalsFromStops<K extends Comparable, V> implements Iterator<ValuedInterval<K,V>> {


    IntervalsStopsIterator<K,V> stopsIterator;
    Map.Entry<K,V> previous;
    Map.Entry<K,V> current;

    public IntervalsFromStops(IntervalsStopsIterator<K,V> stopsIterator) {
        this.stopsIterator = stopsIterator;
        
        // get firt edge
        if (this.stopsIterator.hasNext()) {
            current = this.stopsIterator.next();
        }
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public ValuedInterval<K,V> next() {
        // move to next intervalwith list of values in it.
        previous = current;
        if (this.stopsIterator.hasNext()) {
            current = this.stopsIterator.next();
        } else {
            current = null;
        }

        ValuedInterval<K,V> vi = new ValuedInterval<>(
            new BaseInterval<>(previous.getKey(), current == null ? stopsIterator.upperBound() : current.getKey()),
            previous.getValue()
        );

        return vi;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }

}

    