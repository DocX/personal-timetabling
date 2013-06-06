/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Iterator over IntervalMultimap intervals compatible with IntervalsSet. 
 * Iterates through edges chaning state of "being in some/none interval".
 * @author docx
 */
public class IntervalMultimapSetIterator<K extends Comparable, V> implements Iterator<Entry<K, Boolean>>  {

    IntervalMultimap<K,V> multimap;
    
    Iterator<Entry<K,List<V>>> multimapEdgesIterator;
    
    Entry<K,List<V>> currentMultimapEdge;

    public IntervalMultimapSetIterator(IntervalMultimap<K,V> multimap) {
        this.multimap = multimap;
        multimapEdgesIterator = multimap.edges.entrySet().iterator();
        currentMultimapEdge = null;
    }
    
    
    
    @Override
    public boolean hasNext() {
        // if is in interval, it sure has to have next change to outside interval
        // if is outside, and some edges in multiset are ahead, it sure has next change
        return multimapEdgesIterator.hasNext();
    }

    @Override
    public Entry<K, Boolean> next() {
        // if currently is outside interval
        if (currentMultimapEdge == null || currentMultimapEdge.getValue().isEmpty()) {
            currentMultimapEdge = multimapEdgesIterator.next();
            return new AbstractMap.SimpleEntry<>(currentMultimapEdge.getKey(), true);
        } else {
            // currently inside interval, search for first out of interval edge
            while (!currentMultimapEdge.getValue().isEmpty()) {
                currentMultimapEdge = multimapEdgesIterator.next();
            }
            return new AbstractMap.SimpleEntry<>(currentMultimapEdge.getKey(), false);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported for this iterator.");
    }
    
}
