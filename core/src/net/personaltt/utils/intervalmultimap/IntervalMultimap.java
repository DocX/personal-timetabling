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
public class IntervalMultimap<V> {
    
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
    TreeMap<Integer, MultimapEdge<V>> edges;
    
    /**
     * Lookup structure for keeping information about where interval
     * of some value starts.
     */
    HashMap<V, Integer> startPoints;
    
    public IntervalMultimap() {
        edges = new TreeMap<>();
        startPoints = new HashMap<>();
    }
    
    public long put(V value, BaseInterval<Integer> interval) {
        return put(value, interval, null);
    }
    
    /**
     * Adds interval with given value. Return sum of conflicting length for added
     * interval
     * @param value
     * @param interval 
     */
    public long put(V value, BaseInterval<Integer> interval, List<V> conflictingValues) {       
        startPoints.put(value, interval.getStart());
        
        // creates first edge stub (without this value) for interval, if this edge do not exists yet
        MultimapEdge<V> startEdge = addEdge(interval.getStart());
        
        // creates end edge stub (without this value) for interval, if not exists yet
        MultimapEdge<V> endEdge = addEdge(interval.getEnd());
        
        if (conflictingValues != null) {
            conflictingValues.addAll(startEdge.values);
            conflictingValues.removeAll(startEdge.addedValues);
        }
        
        long newConflictSum = 0;
        List<V> previousEdgeValues = null;
        Integer previousEdge = 0;
        
        // add value to all edges including start to excluding end
        for(Entry<Integer,MultimapEdge<V>> edge : edges.subMap(interval.getStart(), interval.getEnd()).entrySet()) {
            // copy conflicting values
            if (conflictingValues != null) {
                conflictingValues.addAll(edge.getValue().addedValues);
            }
            
            // add to all intermediate edges
            edge.getValue().values.add(value);
            
            // sum conflict from previous elementary interval
            if (previousEdgeValues != null) {
                newConflictSum += (previousEdgeValues.size() - 1) * (long)(edge.getKey() - previousEdge) * 2l;
            }
            
            previousEdgeValues = edge.getValue().values;
            previousEdge = edge.getKey();
        }
        
        // add sum for last elementaery interval
        newConflictSum += (previousEdgeValues.size() - 1) * (long)(interval.getEnd() - previousEdge) * 2l;
        
        
        // add to changing lists of first and last edge
        startEdge.addedValues.add(value);
        endEdge.removedValues.add(value);
        
        return newConflictSum;
    }
    
    /**
    * Adds edge in given key if not exists. New edge has copied values from 
    * edge before it, so it actually not change state.
    * If edge in given key exists, does nothing
    * @param key 
    */
    private MultimapEdge<V> addEdge(Integer key) {
        Entry<Integer, MultimapEdge<V>> floorKey = edges.floorEntry(key);
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
    public long remove(V value) {
        // get start ofgiven value
        Integer start = startPoints.remove(value);
        
        // if non existing value is removed, return
        if (start == null) {
            return 0;
        }
        
        long conflictingSum = 0;
        List<V> prevElementValues;
        int prevElementStart;
        
        // go throug edges and remove value from all 
        // edges from start to first edge without this value
        // and remove edges that has now same values as previous
        Iterator<Entry<Integer,MultimapEdge<V>>> it = edges.tailMap(start).entrySet().iterator();
        Entry<Integer,MultimapEdge<V>> edge = it.next();
        
        edge.getValue().values.remove(value);
        
        prevElementValues = edge.getValue().values;
        prevElementStart = edge.getKey();
        
        // if start edge now dont change anything - added and removed list is empty
        // remove edge
        edge.getValue().addedValues.remove(value);
        if (edge.getValue().hasNoChange()) {
            it.remove();
        }       
        
        while(it.hasNext()) {
            edge = it.next();
            
            // compute conflicting sum difference that will be removed
            conflictingSum += (prevElementValues.size()) * (long)(edge.getKey() - prevElementStart) * 2l;
            prevElementValues = edge.getValue().values;
            prevElementStart = edge.getKey();
            
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
        
        return conflictingSum;
    }
    
    
    
   
    /**
     * Values interval of given point. Point p is in interval i iff e_i leq p le e_i+1..
     * Returns null if it lies outside intervals multimap boudary (before first interval
     * or after last interval)
     * @param point
     * @return 
     */
    public ValuedInterval<Integer,List<V>> valuesIntervalOfPoint(Integer point) {
        Entry<Integer, MultimapEdge<V>> startEntry = edges.floorEntry(point);
        Integer endEdge = edges.higherKey(point);
        
        // if it lies before any interval, return null
        if (startEntry == null || endEdge == null) {
            return null;
        }
        
        ValuedInterval<Integer,List<V>> vi = new ValuedInterval<>(
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
    public Iterable<ValuedInterval<Integer,List<V>>> valuesInInterval(final BaseInterval<Integer> interval) {
        
        
        // otherwise make stops iterator in edges boundary and wrap it in intervals
        // from stops iterator
        return new Iterable<ValuedInterval<Integer, List<V>>>() {
            @Override
            public Iterator<ValuedInterval<Integer, List<V>>> iterator() {
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
    public Iterable<ValuedInterval<Integer,MultimapEdge<V>>> valuesChangesInInterval(final BaseInterval<Integer> interval) {
        
        
        // otherwise make stops iterator in edges boundary and wrap it in intervals
        // from stops iterator
        return new Iterable<ValuedInterval<Integer, MultimapEdge<V>>>() {
            @Override
            public Iterator<ValuedInterval<Integer, MultimapEdge<V>>> iterator() {
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
    public Iterable<ValuedInterval<Integer, List<V>>> valuesIntervals() {
        return valuesInInterval(new BaseInterval<>(edges.firstKey(), edges.lastKey()));
    }
    
    
    public Iterable<Entry<Integer,MultimapEdge<V>>> stopsInMap() {
        return edges.entrySet();
    }
    
    /**
     * Iterator over elementary intervals start points of given interval. elementary intervals
     * starts are t_0, t_1, t_2 ..., t_n. They covers given interval so that
     * [t_0, t_1) union [t_1, t_2) ... [t_n, interval_end) = interval.
     * @param interval
     * @return 
     */
    public Iterator<Entry<Integer,MultimapEdge<V>>> edgesIteratorInInterval(BaseInterval<Integer> interval) {
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
    public Iterable<ValuedInterval<Integer, List<V>>> intervalsIn(final V value) {
        return new Iterable<ValuedInterval<Integer, List<V>>>() {

            @Override
            public Iterator<ValuedInterval<Integer, List<V>>> iterator() {
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
    private class MultimapSubsetEdgeIterator implements IntervalsStopsIterator<Integer,MultimapEdge<V>> {

        BaseInterval<Integer> boundary;
        Iterator<Entry<Integer,MultimapEdge<V>>> subsetIterator;
        
        Entry<Integer, MultimapEdge<V>> current;

        public MultimapSubsetEdgeIterator(BaseInterval<Integer> boundary) {
            this.boundary = boundary;
            
            // set first step - it is always boundary start,
            // only determine if it is before edges map or inside
            Entry<Integer, MultimapEdge<V>> floor = edges.floorEntry(boundary.getStart());
            if (floor == null) {
                current = new AbstractMap.SimpleEntry<Integer, MultimapEdge<V>>(boundary.getStart(), new MultimapEdge<V>());
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
        public Entry<Integer,MultimapEdge<V>> next() {
            Entry<Integer, MultimapEdge<V>> ret = current;
            
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

        public Integer upperBound() {
            return boundary.getEnd();
        }
        
    }

    private class MultimapSubsetStopsIterator implements IntervalsStopsIterator<Integer,List<V>> {

        MultimapSubsetEdgeIterator edgesIterator;
        
        public MultimapSubsetStopsIterator(BaseInterval<Integer> boundary) {
            edgesIterator = new MultimapSubsetEdgeIterator(boundary);
        }     
        
        @Override
        public Integer upperBound() {
            return edgesIterator.upperBound();
        }

        @Override
        public boolean hasNext() {
            return edgesIterator.hasNext();
        }

        @Override
        public Entry<Integer, List<V>> next() {
            Entry<Integer,MultimapEdge<V>> next = edgesIterator.next();
            return new AbstractMap.SimpleEntry<>(next.getKey(), next.getValue().values);
        }

        @Override
        public void remove() {
            edgesIterator.remove();
        }
        
    }
    
    private class StopsInValueInterval implements IntervalsStopsIterator<Integer,List<V>>  {
        V value;
        Iterator<Entry<Integer,MultimapEdge<V>>> subsetIterator;
        
        Entry<Integer, MultimapEdge<V>> current;

        public StopsInValueInterval(V value) {
            this.value = value;
            
            Integer startPoint = startPoints.get(value);
            subsetIterator = edges.tailMap(startPoint, true).entrySet().iterator();
            current = subsetIterator.next();
        }

        @Override
        public boolean hasNext() {
            // it has next if next is not removing it
            return !current.getValue().removedValues.contains(value);
        }

        @Override
        public Entry<Integer, List<V>> next() {
            Entry<Integer, List<V>> ret = new AbstractMap.SimpleEntry<Integer, List<V>>(current.getKey(), current.getValue().values);;;
            
            current = subsetIterator.next();
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Integer upperBound() {
            if (!current.getValue().removedValues.contains(value)) {
                throw new UnsupportedOperationException("Stops iterator do not iterate to bound yet.");
            }
            
            return current.getKey();
        }
        
    }
    
}
