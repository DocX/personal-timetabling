/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils.intervalmultimap;

import java.util.AbstractCollection;
import java.util.AbstractList;
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
import net.personaltt.model.Occurrence;
import net.personaltt.timedomain.IntervalsSet;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.ValuedInterval;

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
    
    public static class MultimapEdge<V> { 
        /**
         * List of values existing in interval from this edge to the next edge
         */
        List<V> values;
        /**
         * List of changed added values in this edge
         */
        List<V> addedValues;
        /**
         * List of changed removed values in this edge
         */
        List<V> removedValues;

        public MultimapEdge() {
            values = new ArrayList<>();
            addedValues = new ArrayList<>();
            removedValues = new ArrayList<>();
        }
        
        /**
         * Returns copy which contains same values. Changes list are empty
         * @return 
         */
        MultimapEdge<V> copy() {
            MultimapEdge<V> copy = new MultimapEdge<>();
            copy.values = new ArrayList<>(this.values);
            copy.removedValues = new ArrayList<>();
            copy.addedValues = new ArrayList<>();
            return copy;
        }

        private boolean hasNoChange() {
            return removedValues.isEmpty() && addedValues.isEmpty();
        }
        
        /**
         * Size of values in the edges interval
         * @return 
         */
        public int size() {
            return values.size();
        }
        
        /**
         * Value of interval in given index
         * @param index
         * @return 
         */
        public V get(int index) {
            return values.get(index);
        }
        
        public List<V> getValues() {
            return values;
        }
        
        public Object[] toArray() {
            return values.toArray();
        }
        
        public List<V> getAdded() {
            return addedValues;
        }
    }
    
    /** 
     * Sorted structure for keeping intervals edges and values on them.
     * Edges with empty list are ending edges.
     */
    TreeMap<K, MultimapEdge<V>> edges;
    
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
        MultimapEdge<V> startEdge = addEdge(interval.getStart());
        
        // creates end edge stub (without this value) for interval, if not exists yet
        MultimapEdge<V> endEdge = addEdge(interval.getEnd());
        
        // add value to all edges including start to excluding end
        int newOverlappings = 0;
        HashSet<V> overlappings = new HashSet<>(startEdge.values.size() * 2);
        for(Entry<K,MultimapEdge<V>> edge : edges.subMap(interval.getStart(), interval.getEnd()).entrySet()) {
            
            // add this to overlapping list of all values in edge
            for (V v : edge.getValue().values) {
                Set<V> vOverlappings = overlappingValues.get(v);
                // if value has no overlapping now, add it as new conflict
                if (vOverlappings.isEmpty()){
                    newOverlappings += 1;
                }                    
                vOverlappings.add(value);
                overlappings.add(v);
            }
            
            // add to all intermediate edges
            edge.getValue().values.add(value);
        }
        
        // add to changing lists of first and last edge
        startEdge.addedValues.add(value);
        endEdge.removedValues.add(value);
        
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
    private MultimapEdge<V> addEdge(K key) {
        Entry<K, MultimapEdge<V>> floorKey = edges.floorEntry(key);
        if(floorKey == null || floorKey.getKey().compareTo(key) != 0) {
            // going to add new edge, but with what values?
            MultimapEdge<V> list;
            if (floorKey != null) {
                //some edge is before, add its values (adding to its territory)
                list = floorKey.getValue().copy();
            } else {
                list = new MultimapEdge<>();
            }
            
            // add new starting edge
            edges.put(key,list);
            return list;
        }
        return floorKey.getValue();
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
        
        // remove it from added values list of start edge
        edges.get(start).addedValues.remove(value);
        
        // if start edge now dont change anythink - added and removed list is empty
        // remove edge
        if (edges.get(start).hasNoChange()) {
            edges.remove(start);
        }
        
        for (Iterator<Entry<K,MultimapEdge<V>>> it = edges.tailMap(start).entrySet().iterator(); it.hasNext();) {
            Entry<K,MultimapEdge<V>> edge = it.next();
            
            // until it is not edge that removes removing value, 
            // remove value from the list of values in the edge

            // remove value from edge list
            edge.getValue().values.remove(value);
            
            if (edge.getValue().removedValues.contains(value)) {
                edge.getValue().removedValues.remove(value);
                if (edge.getValue().hasNoChange()) {
                    it.remove();
                }
                break;
            }
            // else
            // in the middle of interval is no way to exists reason for
            // removing edge - all edges was introduced for changing state
            // but we are in middle of value's interval, so no change is introduced
            // in that point by it

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
        Entry<K, MultimapEdge<V>> startEntry = edges.floorEntry(point);
        K endEdge = edges.higherKey(point);
        
        // if it lies before any interval, return null
        if (startEntry == null || endEdge == null) {
            return null;
        }
        
        ValuedInterval<K,List<V>> vi = new ValuedInterval<>(
            new BaseInterval<>(startEntry.getKey(), endEdge),
            startEntry.getValue().values
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
                return new ElementaryIntervalsIterator(new MultimapSubsetStopsIterator(
                        interval
                        ));
            }
        };
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
    public Iterable<ValuedInterval<K,MultimapEdge<V>>> valuesChangesInInterval(final BaseInterval<K> interval) {
        // if edges are empty, no interval is there, so return abstract empty list
        if (edges.isEmpty()) {
            return Collections.emptyList();
        }
        
        // otherwise make stops iterator in edges boundary and wrap it in intervals
        // from stops iterator
        return new Iterable<ValuedInterval<K, MultimapEdge<V>>>() {
            @Override
            public Iterator<ValuedInterval<K, MultimapEdge<V>>> iterator() {
                return new ElementaryIntervalsIterator(new MultimapSubsetEdgeIterator(
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
    
    
    public Iterable<Entry<K,MultimapEdge<V>>> stopsInMap() {
        return edges.entrySet();
    }
    
    /**
     * Iterator over elementary intervals start points of given interval. elementary intervals
     * starts are t_0, t_1, t_2 ..., t_n. They covers given interval so that
     * [t_0, t_1) union [t_1, t_2) ... [t_n, interval_end) = interval.
     * @param interval
     * @return 
     */
    public Iterator<Entry<K,MultimapEdge<V>>> edgesIteratorInInterval(BaseInterval<K> interval) {
        return new MultimapSubsetEdgeIterator(interval);
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
                return new ElementaryIntervalsIterator<>(new StopsInValueInterval(value));
            }
        };
    }

    public int size() {
        return startPoints.size();
    }
    
    public Iterable<V> keys() {
        return startPoints.keySet();
    }
   
    
    /**
     * Multimap subsets stops iterator. Splits given interval to chunks by 
     * distinct values in multimap. Iterates over all stops strictly before interval
     * end - last stop determine value of interval from it to given interval end.
     * If map do not containt lower bound for given interval, it will serve 
     * empty edge.
     */
    private class MultimapSubsetEdgeIterator implements IntervalsStopsIterator<K,MultimapEdge<V>> {

        BaseInterval<K> boundary;
        Iterator<Entry<K,MultimapEdge<V>>> subsetIterator;
        
        Entry<K, MultimapEdge<V>> current;

        public MultimapSubsetEdgeIterator(BaseInterval<K> boundary) {
            this.boundary = boundary;
            
            // set first step - it is always boundary start,
            // only determine if it is before edges map or inside
            Entry<K, MultimapEdge<V>> floor = edges.floorEntry(boundary.getStart());
            if (floor == null) {
                current = new AbstractMap.SimpleEntry<K, MultimapEdge<V>>(boundary.getStart(), new MultimapEdge<V>());
            } else {
                current = new AbstractMap.SimpleEntry<>(boundary.getStart(), floor.getValue());
            }
            
            // all stops is between first and boundary end.
            // if map contains key equal to boundary start, skip it (is already in current)
            // if map contains key eqaul to boundary end, also skip it
            subsetIterator = edges.subMap(current.getKey(),false, boundary.getEnd(), false).entrySet().iterator();
        }
        
        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Entry<K,MultimapEdge<V>> next() {
            Entry<K, MultimapEdge<V>> ret = current;
            
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

        public K upperBound() {
            return boundary.getEnd();
        }
        
    }

    private class MultimapSubsetStopsIterator implements IntervalsStopsIterator<K,List<V>> {

        MultimapSubsetEdgeIterator edgesIterator;
        
        public MultimapSubsetStopsIterator(BaseInterval<K> boundary) {
            edgesIterator = new MultimapSubsetEdgeIterator(boundary);
        }     
        
        @Override
        public K upperBound() {
            return edgesIterator.upperBound();
        }

        @Override
        public boolean hasNext() {
            return edgesIterator.hasNext();
        }

        @Override
        public Entry<K, List<V>> next() {
            Entry<K,MultimapEdge<V>> next = edgesIterator.next();
            return new AbstractMap.SimpleEntry<>(next.getKey(), next.getValue().values);
        }

        @Override
        public void remove() {
            edgesIterator.remove();
        }
        
    }
    
    private class StopsInValueInterval implements IntervalsStopsIterator<K,List<V>>  {
        V value;
        Iterator<Entry<K,MultimapEdge<V>>> subsetIterator;
        
        Entry<K, MultimapEdge<V>> current;

        public StopsInValueInterval(V value) {
            this.value = value;
            
            K startPoint = startPoints.get(value);
            subsetIterator = edges.tailMap(startPoint, true).entrySet().iterator();
            current = subsetIterator.next();
        }

        @Override
        public boolean hasNext() {
            // it has next if next is not removing it
            return !current.getValue().removedValues.contains(value);
        }

        @Override
        public Entry<K, List<V>> next() {
            Entry<K, List<V>> ret = new AbstractMap.SimpleEntry<K, List<V>>(current.getKey(), current.getValue().values);;;
            
            current = subsetIterator.next();
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public K upperBound() {
            if (!current.getValue().removedValues.contains(value)) {
                throw new UnsupportedOperationException("Stops iterator do not iterate to bound yet.");
            }
            
            return current.getKey();
        }
        
    }
    
}
