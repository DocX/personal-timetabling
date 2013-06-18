/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.AbstractCollection;
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
 * Interval multimap maps values to its intervals and provides searching
 * through intervals for split intervals of overlapping values 
 * in O(n log n).
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
        Entry<K, List<V>> floorKey = edges.floorEntry(key);
        if(floorKey == null || floorKey.getKey().compareTo(key) != 0) {
            // going to add new edge, but with what values?
            List<V> list = new ArrayList<>();
            if (floorKey != null) {
                //some edge is before, add its values (adding to its territory)
                list.addAll(floorKey.getValue());
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
            previousEdge = edges.lowerEntry(start).getValue();
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
                    
                 }
                 break;
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
     * Values interval of given point. Point p is in interval i iff e_i leq p le e_i+1..
     * Returns null if it lies outside intervals multimap boudary (before first interval
     * or after last interval)
     * @param point
     * @return 
     */
    public ValuedInterval<K,List<V>> valuesIntervalOfPoint(K point) {
        Entry<K, List<V>> startEntry = edges.floorEntry(point);
        K endEdge = edges.higherKey(point);
        
        // if it lies before any interval, return null
        if (startEntry == null || endEdge == null) {
            return null;
        }
        
        ValuedInterval<K,List<V>> vi = new ValuedInterval<>(
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
    public Iterable<ValuedInterval<K,List<V>>> valuesInInterval(final BaseInterval<K> interval) {
        // if edges are empty, no interval is there, so return abstract empty list
        if (edges.isEmpty()) {
            return Collections.emptyList();
        }
        
        // otherwise make stops iterator in edges boundary and wrap it in intervals
        // from stops iterator
        return new Iterable<ValuedInterval<K, List<V>>>() {
            @Override
            public Iterator<ValuedInterval<K, List<V>>> iterator() {
                return new IntervalsFromStops(new MultimapSubsetStopsIterator(
                        interval
                        ));
            }
        };
    }

     /**
     * Values intervals iterator. Iterates over intervals with list of values on it
     * @return 
     */
    public Iterable<ValuedInterval<K, List<V>>> valuesIntervals() {
        return valuesInInterval(new BaseInterval<>(edges.firstKey(), edges.lastKey()));
    }
    
    public Iterable<Entry<K,List<V>>> stopsInMap() {
        return edges.entrySet();
    }
    
    
    /**
     * Iterable over maped values to intervals
     * @return 
     */
    public Iterable<V> values() {
        return startPoints.keySet();
    }

    
    /**
     * Iterable over intervals chunks spliting interval of given value
     * @param value
     * @return 
     */
    public Iterable<ValuedInterval<K, List<V>>> intervalsIn(final V value) {
        return new Iterable<ValuedInterval<K, List<V>>>() {

            @Override
            public Iterator<ValuedInterval<K, List<V>>> iterator() {
                return new IntervalsFromStops<>(new StopsInValueInterval(value));
            }
        };
    }

    public int size() {
        return startPoints.size();
    }
    
    
    /**
     * Multimap subsets stops iterator. Splits given interval to chunks by 
     * distinct values in multimap. Iterates over all stops strictly before interval
     * end - last stop determine value of interval from it to given interval end
     */
    private class MultimapSubsetStopsIterator implements IntervalsStopsIterator<K,List<V>> {

        BaseInterval<K> boundary;
        Iterator<Entry<K,List<V>>> subsetIterator;
        
        Entry<K, List<V>> current;

        public MultimapSubsetStopsIterator(BaseInterval<K> boundary) {
            this.boundary = boundary;
            
            // set first step - it is always boundary start,
            // only determine if it is before edges map or inside
            Entry<K, List<V>> floor = edges.floorEntry(boundary.start);
            if (floor == null) {
                current = new AbstractMap.SimpleEntry<K, List<V>>(boundary.start, new ArrayList<V>());
            } else {
                current = new AbstractMap.SimpleEntry<>(boundary.start, floor.getValue());
            }
            
            // all stops is between first and boundary end.
            // if map contains key equal to boundary start, skip it (is already in current)
            // if map contains key eqaul to boundary end, also skip it
            subsetIterator = edges.subMap(current.getKey(),false, boundary.end, false).entrySet().iterator();
        }
        
        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Entry<K,List<V>> next() {
            Entry<K, List<V>> ret = current;
            
            if (subsetIterator.hasNext()) {
                current = subsetIterator.next();
            } else {
                current = null;
            }
            
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public K upperBound() {
            return boundary.end;
        }
        
    }

    private class StopsInValueInterval implements IntervalsStopsIterator<K,List<V>>  {
        V value;
        Iterator<Entry<K,List<V>>> subsetIterator;
        
        Entry<K, List<V>> current;

        public StopsInValueInterval(V value) {
            this.value = value;
            
            K startPoint = startPoints.get(value);
            subsetIterator = edges.tailMap(startPoint, true).entrySet().iterator();
            current = subsetIterator.next();
        }

        @Override
        public boolean hasNext() {
            return current.getValue().contains(value);
        }

        @Override
        public Entry<K, List<V>> next() {
            Entry<K, List<V>> ret = current;
            
            current = subsetIterator.next();
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public K upperBound() {
            if (current.getValue().contains(value)) {
                throw new UnsupportedOperationException("Stops iterator do not iterate to bound yet.");
            }
            
            return current.getKey();
        }
        
    }
    
}
