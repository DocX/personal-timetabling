/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author docx
 */
public class FilteringIntervalMultimapSetIterator<K extends Comparable, V> implements Iterator<Map.Entry<K, Boolean>>  {

    IntervalMultimap<K,V> multimap;
    
    Iterator<Map.Entry<K,List<V>>> multimapEdgesIterator;
    
    /**
     * count of values on previous multimap edge. From start there is virtual
     * edge with zero values in the negative infinity
     */
    int currentMultimapEdgeCount = 0;
    
    /**
     * storage of next edge
     */
    Map.Entry<K,Boolean> nextEdge;
    
    /**
     * exclusive boundary number of values in interval of intervals to include in iterator
     */
    int thresold;

    public FilteringIntervalMultimapSetIterator(IntervalMultimap<K,V> multimap, int thresold) {
        this.multimap = multimap;
        multimapEdgesIterator = multimap.edges.entrySet().iterator();
        this.thresold = thresold;
        
        findNext();
    }
    
    
    
    @Override
    public boolean hasNext() {
        // if is in interval, it sure has to have next change to outside interval
        // if is outside, and some edges in multiset are ahead, it sure has next change
        return nextEdge != null;
    }

    @Override
    public Map.Entry<K, Boolean> next() {
        Map.Entry<K,Boolean> result = nextEdge;
        findNext();
        
        return result;
    }
    
    private void findNext() {       
        while (multimapEdgesIterator.hasNext()) {
            Map.Entry<K,List<V>> multimapEdge = multimapEdgesIterator.next();
            
            // if edge has more values and is after edge with less values (starts new interval)
            if (multimapEdge.getValue().size() > thresold && currentMultimapEdgeCount <= thresold) {
                nextEdge = new AbstractMap.SimpleEntry<>(multimapEdge.getKey(), Boolean.TRUE);
                currentMultimapEdgeCount = multimapEdge.getValue().size();
                return;
            }
            
            if (multimapEdge.getValue().size() <= thresold && currentMultimapEdgeCount > thresold) {
                nextEdge = new AbstractMap.SimpleEntry<>(multimapEdge.getKey(), Boolean.FALSE);
                currentMultimapEdgeCount = multimapEdge.getValue().size();
                return;
            }
        }
        
        nextEdge = null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported for this iterator.");
    }
    
}