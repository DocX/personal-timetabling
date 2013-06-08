/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
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
     * Sorted structure for keeping intervals edges and values on them
     */
    TreeMap<K, List<V>> edges;
    
    /**
     * Lookup structure for keeping information about where interval
     * of some value starts.
     */
    HashMap<V, K> startPoints;

    public IntervalMultimap() {
        edges = new TreeMap<>();
        startPoints = new HashMap<>();
    }
    
    
    /**
     * Adds interval with given value
     * @param value
     * @param interval 
     */
    public boolean put(V value, BaseInterval<K> interval) {
        // if adding value already exists in multiset, throw exception
        if (startPoints.containsKey(value)) {
            throw new IllegalArgumentException("Adding value already exists or is evaluated as eqaual to already existing value."); 
        }
        
        startPoints.put(value, interval.getStart());
        
        // creates first edge stub (without this value) for interval, if this edge do not exists yet
        createEdgeStub(interval.getStart());
        
        // creates end edge stub (without this value) for interval, if not exists yet
        createEdgeStub(interval.getEnd());
        
        // add value to all edges including start to excluding end
        boolean conflicts = false;
        for(List<V> edgeValues : edges.subMap(interval.getStart(), interval.getEnd()).values()) {
            if (!conflicts && !edgeValues.isEmpty()) {
                conflicts = true;
            }
            edgeValues.add(value);
        }
        
        return conflicts;
    }
    
    /**
     * Adds all entries in collection
     * @param collection
     * @return 
     */
    public boolean putAll(Iterable<Entry<V, BaseInterval<K>>> collection) {
        boolean conflicts = false;
        for (Entry<V, BaseInterval<K>> entry : collection) {
            conflicts |= this.put(entry.getKey(), entry.getValue());
        }
        
        return conflicts;
    }
    
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
    
    /**
     * Removes interval for given value
     * @param value 
     */
    public void remove(V value) {
        // get start ofgiven value
        K start = startPoints.remove(value);
        
        // if non existing value is removed, return
        if (start == null) {
            return;
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
    }
    
    
    
    /**
     * Get list of values (intervals) in given point
     * @param point
     * @return 
     */
    public List<V> valuesIn(K point) {
        return edges.floorEntry(point).getValue();
    }
    
    /**
     * Retrievs values of all intervals that overlaps (touches) other interval.
     * List has non deterministic order
     * @return 
     */
    public ArrayList<V> valuesOfOverlappingIntervals() {
        // for each edge that have more than one value, record these values as 
        // overlapping.
        // Because values on the edge means "intervals values that are from the moment of the
        // edge at least to the moment of next edge parallel"
        
        Set<V> overlappingIntervalsValues = new HashSet<>();
        
        for (List<V> edgeValues : edges.values()) {
            if (edgeValues.size() > 1) {
                overlappingIntervalsValues.addAll(edgeValues);
            }
        }
        
        return new ArrayList<>(overlappingIntervalsValues);
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
