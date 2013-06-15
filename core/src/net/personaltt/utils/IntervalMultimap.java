/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import net.personaltt.problem.Occurrence;
import net.personaltt.timedomain.IntervalsSet;

/**
 * Interval multimap stores intervals with mapped value and provides searching
 * through intervals for all mapped values in given interval in O(log n).
 * Intervals are identified by its value.
 * 
 * It stores intervals edges in sorted structure (like binary tree) and maps them to
 * the list of values that are from edge point to the right. Each value is for each
 * original interval.
 * 
 * Structure provides this actions
 * - set operations with intervals set
 * - find all intervals in given point or interval and its values
 * - add and remove intervals
 * 
 * @author docx
 */
public class IntervalMultimap<K extends Comparable, V> {
    
    /** 
     * Sorted structure for keeping intervals edges and values on them.
     * Edges with empty list are ending edges.
     */
    TreeMap<K, List<V>> edges;
    
    /**
     * Lookup structure for keeping information about where interval
     * of some value starts.
     */
    HashMap<V, K> startPoints;
    
    /**
     * Overlapping values map. Map of values to list of other values that overlaps.
     */
    HashMap<V, Set<V>> overlappingValues;

    public IntervalMultimap() {
        edges = new TreeMap<>();
        startPoints = new HashMap<>();
        overlappingValues = new HashMap<>();
    }
    
    
    /**
     * Adds interval with given value. Retrun number of distinct values that was in 
     * multimap before adding, that are now conflicting due to this addition
     * @param value
     * @param interval 
     */
    public int put(V value, BaseInterval<K> interval) {
        // if adding value already exists in multiset, throw exception
        if (startPoints.containsKey(value)) {
            return 0;
        }
        
        startPoints.put(value, interval.getStart());
        
        // creates first edge stub (without this value) for interval, if this edge do not exists yet
        addEdge(interval.getStart());
        
        // creates end edge stub (without this value) for interval, if not exists yet
        addEdge(interval.getEnd());
        
        // add value to all edges including start to excluding end
        int newOverlappings = 0;
        HashSet<V> overlappings = new HashSet<>();
        for(Entry<K,List<V>> edge : edges.subMap(interval.getStart(), interval.getEnd()).entrySet()) {
            // add this to overlapping list of all values in edge
            for (V v : edge.getValue()) {
                Set<V> vOverlappings = overlappingValues.get(v);
                // if value has no overlapping now, add it as new conflict
                if (vOverlappings.isEmpty()){
                    newOverlappings += 1;
                }                    
                vOverlappings.add(value);
                overlappings.add(v);
            }
            
            edge.getValue().add(value);
        }
        
        overlappingValues.put(value, overlappings);
        
        if (overlappings.isEmpty() == false) {
            // adds one also for current added, if is overlapping
            newOverlappings += 1;
        }
        return newOverlappings;
    }
    
    /**
    * Adds edge in given key if not exists. New edge has copied values from 
    * edge before it, so it actually not change state.
    * If edge in given key exists, does nothing
    * @param key 
    */
    private void addEdge(K key) {
        K floorKey = edges.floorKey(key);
        if(floorKey == null || !floorKey.equals(key)) {
            // going to add new edge, but with what values?
            List<V> list = new ArrayList<>();
            if (floorKey != null) {
                //some edge is before, add its values (adding to its territory)
                list.addAll(edges.get(floorKey));
            }
            
            // add new starting edge
            edges.put(key,list);
        }
    }
    
    /**
     * Removes interval for given value. Returns number of other values, which are
     * after removal with no conflict and before it with conflict.
     * @param value 
     */
    public int remove(V value) {
        // get start ofgiven value
        K start = startPoints.remove(value);
        
        // if non existing value is removed, return
        if (start == null) {
            return 0;
        }
        
        // go throug edges and remove value from all 
        // edges from start to first edge without this value
        // and remove edges that has now same values as previous
        
        List<V> previousEdge = new ArrayList<>();
        if (edges.lowerKey(start) != null) {
            edges.lowerEntry(start).getValue();
        }
        
        for (Iterator<Entry<K,List<V>>> it = edges.tailMap(start).entrySet().iterator(); it.hasNext();) {
            Entry<K,List<V>> edge = it.next();
            
            if (edge.getValue().contains(value)) {
                // remove value from edge list
                edge.getValue().remove(value);
                
                // test if has same values as previous
                if (edge.getValue().size() == previousEdge.size() && edge.getValue().containsAll(previousEdge)) {
                    it.remove();
                } else {
                    previousEdge = edge.getValue();
                }
            } else {
                // fist edge that do not contain given value, so it is the end of value's interval
                // if it doesn contain any other value, remove edge
                 if (edge.getValue().size() == previousEdge.size() && edge.getValue().containsAll(previousEdge)) {
                    it.remove();
                    break;
                }
            }
        }
        
        // removes from overlappings for values that overlaps removing value
        int newNonOverlapping = 0;
        for (V v : overlappingValues.get(value)) {
            Set<V> valueOverlappings = overlappingValues.get(v);
            boolean contained = valueOverlappings.remove(value);
            
            if (contained && valueOverlappings.isEmpty()) {
                newNonOverlapping += 1;
            }
        }
        
       
        if (overlappingValues.remove(value).isEmpty() == false) {
            // removes one also for removed, if was overlapping
            newNonOverlapping += 1;
        }
        return newNonOverlapping;
    }
    
    
    /**
     * Values intervals iterator. Iterates over intervals with list of values on it
     * @return 
     */
    public Iterable<ValuesInterval> valuesIntervals() {
        return new ValuesIntervalsIterable(this);
    }
    
    /**
     * Values interval of given point. Point p is in interval i iff e_i leq p le e_i+1..
     * Returns null if it lies outside intervals multimap boudary (before first interval
     * or after last interval)
     * @param point
     * @return 
     */
    public ValuesInterval valuesIntervalOfPoint(K point) {
        Entry<K, List<V>> startEntry = edges.floorEntry(point);
        K endEdge = edges.higherKey(point);
        
        // if it lies before any interval, return null
        if (startEntry == null || endEdge == null) {
            return null;
        }
        
        ValuesInterval vi = new ValuesInterval(
            new BaseInterval<>(startEntry.getKey(), endEdge),
            startEntry.getValue()
                );
        return vi;
    }
    
    /**
     * Values in interval. Splits given interval to chunks by values changes in
     * current multimap on that intervals.
     * Retuns list of chunks with values in chunk interval. First chunk has start to 
     * start of given interval and last chunk ends on given interval end. Whole
     * interval range is covered by chunks.
     * @param interval
     * @return 
     */
    public List<ValuesInterval> valuesInInterval(BaseInterval<K> interval) {
        List<ValuesInterval> values = new ArrayList<>();
        
        if(edges.isEmpty()) {
            return values;
        }
        
        Entry<K, List<V>> floorEdge = edges.floorEntry(interval.start);
        if (floorEdge == null) {
            floorEdge = edges.firstEntry();
            values.add(new ValuesInterval(new BaseInterval<>(interval.start, floorEdge.getKey()), new ArrayList<V>()));
        }
        
        for (Entry<K, List<V>> entry : edges.subMap(floorEdge.getKey(),false, interval.end, false).entrySet()) {
            values.add(
                    new ValuesInterval(new BaseInterval<>(
                        floorEdge.getKey().compareTo(interval.start) < 0 ? interval.start : floorEdge.getKey(),
                        entry.getKey().compareTo(interval.end) > 0 ? interval.end : entry.getKey()
                    ),
                    floorEdge.getValue()));
            
            floorEdge = entry;
        }
        
        // add last interval
        K endKey = floorEdge.getKey().compareTo(interval.start) < 0 ? interval.start : floorEdge.getKey();
        Entry<K, List<V>> endEntry = edges.floorEntry(endKey);
        values.add(
                new ValuesInterval(new BaseInterval<>(
                    endKey,
                    interval.end
                ),
                endEntry.getValue()
                )
           );
        
        return values;
    }

    
    /**
     * Interval multimap entry. Stores interval and values that covers it.
     */
    public class ValuesInterval extends BaseInterval<K> {
        List<V> values;

        public List<V> getValues() {
            return values;
        }


        public ValuesInterval(BaseInterval<K> interval, List<V> values) {
            super(interval.start, interval.end);
            this.values = values;
        }
    }
    
    /**
     * Values Intervals iterable. Iterable wrapper around ValuesIntervalsIterator
     */
    private class ValuesIntervalsIterable implements Iterable<ValuesInterval> {

        IntervalMultimap<K,V> multimap;

        public ValuesIntervalsIterable(IntervalMultimap<K, V> multimap) {
            this.multimap = multimap;
        }
        
        @Override
        public Iterator<ValuesInterval> iterator() {
            return new ValuesIntervalsIterator(multimap);
        }
        
    }
    
    /**
     * Values intervals iterator. Iterates over intervals of distinct values list.
     */
    private class ValuesIntervalsIterator implements Iterator<ValuesInterval> {

        IntervalMultimap<K,V> multimap;
        Iterator<Entry<K,List<V>>> multimapIterator;
        Entry<K,List<V>> previous;
        Entry<K,List<V>> current;

        public ValuesIntervalsIterator(IntervalMultimap<K, V> multimap) {
            this.multimap = multimap;
            multimapIterator = multimap.edges.entrySet().iterator();
            // get firt edge
            if (multimapIterator.hasNext()) {
                current = multimapIterator.next();
            }
        }
        
        @Override
        public boolean hasNext() {
            return multimapIterator.hasNext();
        }

        @Override
        public ValuesInterval next() {
            // move to next intervalwith list of values in it.
            previous = current;
            current = multimapIterator.next();
                        
            ValuesInterval vi = new ValuesInterval(
                new BaseInterval<>(previous.getKey(), current.getKey()),
                previous.getValue()
                        );
            
            // if current is end of interval, skip to next new interval
            if (current.getValue().isEmpty() && multimapIterator.hasNext()) {
                current = multimapIterator.next();
            }

            return vi;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
    
}
