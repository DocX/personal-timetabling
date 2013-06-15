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
        createEdgeStub(interval.getStart());
        
        // creates end edge stub (without this value) for interval, if not exists yet
        createEdgeStub(interval.getEnd());
        
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
     * Retrievs values of all intervals that overlaps (touches) other interval.
     * List has non deterministic order
     * @return 
     */
    public Map<V, Integer> valuesOverlappingSum(KeyDifference<K> diff) {
        // for each edge that have more than one value, record these values as 
        // overlapping.
        // Because values on the edge means "intervals values that are from the moment of the
        // edge at least to the moment of next edge parallel"
        
        Map<V, Integer> overlappingIntervalsValues = new HashMap<>();
        
        Entry<K,List<V>> prev = new AbstractMap.SimpleImmutableEntry<K,List<V>>(null, new ArrayList<V>());
        for (Entry<K,List<V>> edgeValues : edges.entrySet()) {

            if (prev.getValue().size() > 1) {
                // add to each value one
                for (V v : prev.getValue()) {
                    Integer old = overlappingIntervalsValues.get(v);
                    int add = diff.diff(edgeValues.getKey(), prev.getKey()) * prev.getValue().size();
                    overlappingIntervalsValues.put(v, (old == null ? 0 : old.intValue()) + add);
                }
            }

            prev = edgeValues;
        }
        
        // the last "prev" is not need to process, since the last edge is always
        // "ending" edge
        
        return overlappingIntervalsValues;
    }

    /**
     * Return overlapping values with zero sum.
     * @return 
     */
    public Map<V, Integer> valuesOverlappingSum() {
        return valuesOverlappingSum(new KeyDifference<K>() {
            @Override
            public int diff(K a, K b) {
                return 0;
            }
        });
    }
    
    /**
     * List of values that overlaps itself. 
     * @return 
     */
    public List<V> overlappingValues() {
        return new ArrayList<>(valuesOverlappingSum().keySet());
    }
    
    /**
     * Values intervals iterator. Iterates over intervals with list of values on it
     * @return 
     */
    public Iterator<ValuesInterval> valuesIntervals() {
        return new ValuesIntervalsIterator(this);
    }
    
    public class ValuesInterval {
        BaseInterval<K> interval;
        List<V> values;

        public List<V> getValues() {
            return values;
        }

        public BaseInterval<K> getInterval() {
            return interval;
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
                        
            ValuesInterval vi = new ValuesInterval();
            vi.interval = new BaseInterval<>(previous.getKey(), current.getKey());
            vi.values = previous.getValue();
            
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
    
    /**
     * Returns iterator over changing edges
     * @return 
     */
    public Iterator<Entry<K,Boolean>> intervalsSetIterator() {
        return new IntervalMultimapSetIterator(this);
    }
    
    /**
     * Return new intervalsset corresponding to intervals allocated by all of 
     * intervals in this
     * @return 
     */
    public BaseIntervalsSet<K> toIntervalsSet() {
        BaseIntervalsSet<K> intervalsSet = new BaseIntervalsSet<>();
        return intervalsSet.getUnionWith(this.intervalsSetIterator());
    }

    /**
     * Iterate through intervals edges for intervals that have more than
     * intervalsOccurrencesThhresold concurrent values
     * @param intervalsOccurrencesThresold
     * @return 
     */
    public Iterator<Entry<Integer, Boolean>> intervalsSetIterator(int intervalsOccurrencesThresold) {
        return new FilteringIntervalMultimapSetIterator(this, intervalsOccurrencesThresold);
    }
    
    /**
     * Returns list of sorted intervals splitting given intervals set into 
     * pieces where is different values. Union of intervals in the list is
     * equal to given intervals. Each interval in list have list of values in 
     * this multimap in the same interval.
     * @param intervals
     * @return 
     */
    public List<MultiIntervalStop> getIntervalsIn(BaseIntervalsSet<K> intervals) {
        TreeMap<K, ListEdge<V>> intervalsMap = new IntervalsSetMerger<>(
            this.edges.entrySet().iterator(),
            intervals.setMap.entrySet().iterator()
            )
            .merge(new IntervalsSetMerger.MergeFunction<ListEdge<V>, List<V>, Boolean>() {

                @Override
                public ListEdge<V> mergeEdge(ListEdge<V> previous_state, List<V> state_a, Boolean state_b) {
                    ListEdge<V> new_edge = new ListEdge<>();
                    new_edge.edge = state_b == null ? false : state_b.booleanValue();
                    new_edge.list = new_edge.edge ? (state_a == null ? new ArrayList<V>() : state_a) : null;
                    
                    // previous state is not substantial
                    
                    return new_edge;
                }
                
            });
        
        List<MultiIntervalStop> multiIntervals = new ArrayList<>();
        for (Entry<K, ListEdge<V>> entry : intervalsMap.entrySet()) {
            multiIntervals.add(new MultiIntervalStop(entry.getValue().edge, entry.getKey(), entry.getValue().list));
        }
        
        return multiIntervals;
    }
    
    /**
     * Creates edge in given key if not exists. New edge has copied values from 
     * edge before it. If edge in given key exists, does nothing
     * @param key 
     */
    private void createEdgeStub(K key) {
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

    
    public interface KeyDifference<K> {
        public int diff(K a, K b);
    }
       
    
    private class ListEdge<V> {
        List<V> list;
        boolean edge;
    }
    
    /**
     * Multi interval is encapsulation of interval and list of values on it
     */
    public class MultiIntervalStop {
        public boolean isIn;
        public K stop;
        public List<V> values;

        public MultiIntervalStop(boolean in, K stop, List<V> values) {
            this.isIn = in;
            this.stop = stop;
            this.values = values;
        }
    }
    
}
