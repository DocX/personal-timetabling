/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import net.personaltt.problem.Occurrence;

/**
 * Base intervals set generic class. Implements intervals set with set operations
 * with O(log n) search/tests and O(n log n) manipulation and retrieving ordered list of sets in O(n) 
 * @author docx
 */
public class BaseIntervalsSet<T extends Comparable> {
    /**
     * map storing edge dates of intervals. Edge is true if it starts interval to the future from it
     * and false if it ends interval to the future from it.
     */
    protected TreeMap<T, Boolean> setMap;
    
    public BaseIntervalsSet() {
        setMap = new TreeMap<>();
    }
    
    
    
    /**
     * Union with single interval in O(n log n)
     * @param start
     * @param end 
     */
    public void unionWith(T start, T end) {
        
        // remove all edges inside adding interval
        // O(n log n) - find start
        setMap.subMap(start,true, end, true).clear();
        
        // if edge before adding start do not exists or is ending edge, add new start
        // O(log n)
        Map.Entry<T, Boolean> floor = setMap.floorEntry(start);
        if (floor == null || floor.getValue() == false)
        {
            // add start
            setMap.put(start, Boolean.TRUE);
        }
        
        // if edge after adding end do not exists or is starting edge, add new end
        // O(log n)
        Map.Entry<T, Boolean> ceiling = setMap.ceilingEntry(start);
        if (ceiling == null || ceiling.getValue() == true)
        {
            // add start
            setMap.put(end, Boolean.FALSE);
        }
    }

    /**
     * Retrieves interval surounging given point or null if given point is outside any
     * interval
     * @param start
     * @return 
     */
    public BaseInterval<T> getIntervalContaining(T start) {
        
        if (this.setMap.floorEntry(start).getValue()) {
            // inside
            return new BaseInterval<>(this.setMap.floorEntry(start).getKey(), this.setMap.ceilingKey(start));
        } else {
            return null;
        }
    }
    
  
    private interface MergeFunction {    
        /*
         * Determine state of resulting interval set on the edge in given states of two intervals sets
         */
        boolean mergeEdge(boolean state_a, boolean state_b);
    }    
    
   /**
     * Union merge operation
     */
    private class UnionMerge implements MergeFunction {
        @Override
        public boolean mergeEdge(boolean state_a, boolean state_b) {
            return state_a || state_b;
        }    
    }    
    
    /**
     * Unions this with given intervals set
     * @param mask 
     */
    public void unionWith(BaseIntervalsSet<T> mask) {        
       this.merge(mask, new UnionMerge());
    }
   
    /**
     * Returns new intervals set of copy of this united with intervals represented by given iterator
     * @param currentAllocationIntervals
     * @return 
     */
    public BaseIntervalsSet<T> getUnionWith(Iterator<Entry<T, Boolean>> intervalsEdgesIterator) {
        BaseIntervalsSet<T> result = new BaseIntervalsSet<>();
        result.setMap = this.getMerged(intervalsEdgesIterator, new UnionMerge());
        return result;
    }
    
    /**
     * Intersection merge operation
     */
    private class IntersectMerge implements MergeFunction {
        @Override
        public boolean mergeEdge(boolean state_a, boolean state_b) {
            return state_a && state_b;
        }    
    }    
    
    /**
     * Intersects this with given intervals set.
     * @param mask 
     */
    public void intersectWith(BaseIntervalsSet<T> mask) {
        // cases:
        // this: |-----|  |---|   |-----|   |----|   
        // mask:   |--------|   |-----------|
        // rslt:   |---|  |-|     |-----|
        
       this.merge(mask, new IntersectMerge());
    }
    
    /**
     * Returns new intervals set of copy of this intersected with intervals represented by given iterator
     * @param currentAllocationIntervals
     * @return 
     */
    public BaseIntervalsSet<T> getIntersectionWith(Iterator<Entry<T, Boolean>> intervalsEdgesIterator) {
        BaseIntervalsSet<T> result = new BaseIntervalsSet<>();
        result.setMap = this.getMerged(intervalsEdgesIterator, new IntersectMerge());
        return result;
    }
   
    
    /**
    * Minus merge operation
    */
    private class MinusMerge implements MergeFunction {
        @Override
        public boolean mergeEdge(boolean state_a, boolean state_b) {
            return state_a && !state_b;
        }    
    }
    
    /**
     * Subtracts given intervals set from this
     * @param subtrahend_set 
     */
    public void minus(BaseIntervalsSet<T> subtrahend_set) {
        // cases:
        // this: |-----|  |---|   |-----|   |----|     |-----| |-----|
        // minu:   |--------|   |-----------|         
        // rslt: |-|        |-|             |----|     |-----| |-----|
        
        this.merge(subtrahend_set, new MinusMerge());
    }
    
     /**
     * Returns new intervals set of this minus intervals represented by given iterator
     * @param currentAllocationIntervals
     * @return 
     */
    public BaseIntervalsSet<T> getSubtraction(Iterator<Entry<T, Boolean>> intervalsEdgesIterator) {
        BaseIntervalsSet<T> result = new BaseIntervalsSet<>();
        result.setMap = this.getMerged(intervalsEdgesIterator, new MinusMerge());
        return result;
    }
    
    
    /**
     * Interface for delegates creating intervals for extended object
     * @param <T> 
     */
    protected interface IntervalsBuilder<T> {
        public void add(T start, T end);
    }
    
    /**
     * Basic builder implementation for provide default function
     */
    private class BaseIntervalBuilder implements IntervalsBuilder<T> {
        List<BaseInterval<T>> intervals;

        public BaseIntervalBuilder() {
            intervals = new ArrayList<>();
        }
       
        @Override
        public void add(T start, T end) {
            intervals.add(new BaseInterval<>(start, end));
        }
    }
    /**
     * Returns distinct intevals in the interval set ordered from the earliest start
     * @return 
     */
    public List<BaseInterval<T>> getBaseIntervals() {
        BaseIntervalBuilder builder = new BaseIntervalBuilder();
        buildIntervals(builder);
        return builder.intervals;
    }
    
    /**
     * Iterates builder.add for each interval in set
     * @param builder 
     */
    protected void buildIntervals(IntervalsBuilder<T> builder) {
        // go throug map and create intervals as numbers goes
        for (Iterator<Map.Entry<T, Boolean>> it = setMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<T, Boolean> object = it.next();
            // start
            if (object.getValue() && it.hasNext())  {
                Map.Entry<T, Boolean> end = it.next();
                
                builder.add(object.getKey(), end.getKey());
            }
        }
    }
    
    /**
     * Merge this with second given intervalset using given operation
     * @param second
     * @param operation 
     */
    private void merge(BaseIntervalsSet<T> second, MergeFunction operation) {
        this.setMap = this.getMerged(second.setMap.entrySet().iterator(), operation);
    }
    
    /*
     * Merge walk througt edges from both intervals sets (this and given) and calls merge function 
     * implementation for each of them
     */
    private TreeMap<T, Boolean> getMerged(Iterator<Map.Entry<T, Boolean>> second_iterator, MergeFunction operation) {
        Iterator<Map.Entry<T, Boolean>> this_iterator = this.setMap.entrySet().iterator(); 
        
        TreeMap<T, Boolean> result = new TreeMap<>();
        boolean lastResultState = false;
       
        // get first steps
        Map.Entry<T, Boolean> this_edge = this_iterator.hasNext() ? this_iterator.next() : null;
        Map.Entry<T, Boolean> second_edge = second_iterator.hasNext() ? second_iterator.next() : null;
 
        while(this_edge != null || second_edge != null) {
            // intervals are here considered as closed-open - so if edge start, is inside
            // and if edge is end, it is outside.
            boolean this_state;
            boolean second_state;
            T step_time;
            
            // walk first this until is before second or second has no more edges
            if (this_edge != null && (second_edge == null || this_edge.getKey().compareTo(second_edge.getKey()) < 0)) {
                // in this, we are on the edge - if out-in edge, we are in, if in-out, we are out
                this_state = this_edge.getValue();
                step_time = this_edge.getKey();
                
                // in second, we are somewhere between edges, so state is determined by first next edge:
                second_state = second_edge == null ? false : !second_edge.getValue();
                
                this_edge = this_iterator.hasNext() ? this_iterator.next() : null;
            // else walk on second
            } else if (second_edge != null && (this_edge == null || second_edge.getKey().compareTo(this_edge.getKey()) < 0 )) {
                // in second, we are on the edge - if out-in edge, we are in, if in-out, we are out
                second_state = second_edge.getValue();
                step_time = second_edge.getKey();
                
                // in this, we are somewhere between edges, so state is determined by first next edge:
                this_state = this_edge == null ? false : !this_edge.getValue();
                
                second_edge = second_iterator.hasNext() ? second_iterator.next() : null;
            // else must be equal not null
            } else {
                this_state = this_edge.getValue();
                second_state = second_edge.getValue();
                step_time = this_edge.getKey();
                
                // move both
                this_edge = this_iterator.hasNext() ? this_iterator.next() : null;
                second_edge = second_iterator.hasNext() ? second_iterator.next() : null;
            }
            
            // if result of merging current state on the edge is other than current state 
            // in the result interval set, add switch edge
            boolean result_state_of_edge = operation.mergeEdge(this_state, second_state);
            if (result_state_of_edge != lastResultState) {
                result.put(step_time, result_state_of_edge);
                lastResultState = result_state_of_edge;
            }
        }
        
        return result;
    }    
}
