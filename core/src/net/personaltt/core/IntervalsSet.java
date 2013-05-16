/* Intervals set. Represents set of intervals and implements set operations on it
 */
package net.personaltt.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.joda.time.LocalDateTime;

/**
 *
 * @author docx
 */
public class IntervalsSet {

    /**
     * map storing edge dates of intervals. Edge is true if it starts interval to the future from it
     * and false if it ends interval to the future from it.
     */
    TreeMap<LocalDateTime, Boolean> setMap;
    
    public IntervalsSet() {
        setMap = new TreeMap<>();
    }
    
    public IntervalsSet(Interval interval) {
        setMap = new TreeMap<>();
        setMap.put(interval.start, Boolean.TRUE);
        setMap.put(interval.end, Boolean.FALSE);
    }
    
    /**
     * Union with single interval
     * @param interval 
     */
    public void unionWith(Interval interval) {
        this.unionWith(interval.start, interval.end);
    }
    
    /**
     * Union with single interval in O(n log n)
     * @param start
     * @param end 
     */
    public void unionWith(LocalDateTime start, LocalDateTime end) {
        
        // remove all edges inside adding interval
        // O(n log n) - find start
        setMap.subMap(start,true, end, true).clear();
        
        // if edge before adding start do not exists or is ending edge, add new start
        // O(log n)
        Entry<LocalDateTime, Boolean> floor = setMap.floorEntry(start);
        if (floor == null || floor.getValue() == false)
        {
            // add start
            setMap.put(start, Boolean.TRUE);
        }
        
        // if edge after adding end do not exists or is starting edge, add new end
        // O(log n)
        Entry<LocalDateTime, Boolean> ceiling = setMap.ceilingEntry(start);
        if (ceiling == null || ceiling.getValue() == true)
        {
            // add start
            setMap.put(end, Boolean.FALSE);
        }
    }  
    
  
    
   /**
     * Intersection merge operation
     */
    private class UnionMerge implements MergeFunction {
        @Override
        public boolean mergeEdge(boolean state_a, boolean state_b) {
            return state_a || state_b;
        }    
    }    
    
    public void unionWith(IntervalsSet mask) {        
       this.merge(mask, new UnionMerge());
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
    
    public void intersectWith(IntervalsSet mask) {
        // cases:
        // this: |-----|  |---|   |-----|   |----|   
        // mask:   |--------|   |-----------|
        // rslt:   |---|  |-|     |-----|
        
       this.merge(mask, new IntersectMerge());
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

    public void minus(Interval subtrahend){
        IntervalsSet s = new IntervalsSet();
        s.unionWith(subtrahend);
        this.minus(s);
    }
    
    public void minus(IntervalsSet subtrahend_set) {
        // cases:
        // this: |-----|  |---|   |-----|   |----|     |-----| |-----|
        // minu:   |--------|   |-----------|         
        // rslt: |-|        |-|             |----|     |-----| |-----|
        
        this.merge(subtrahend_set, new MinusMerge());
    }
    
    
    public List<Interval> getIntervals() {
        ArrayList<Interval> intervals = new ArrayList<>();
        
        // go throug map and create intervals as numbers goes
        Interval i = null;
        for (Iterator<Entry<LocalDateTime, Boolean>> it = setMap.entrySet().iterator(); it.hasNext();) {
            Entry<LocalDateTime, Boolean> object = it.next();
            // start
            if (object.getValue() && it.hasNext())  {
                Entry<LocalDateTime, Boolean> end = it.next();
                
                intervals.add(new Interval(object.getKey(), end.getKey()));
            }
        }
        
        return intervals;
    }
    
    
    private interface MergeFunction {    
        /*
         * Determine state of resulting interval set on the edge in given states of two intervals sets
         */
        boolean mergeEdge(boolean state_a, boolean state_b);
    }
    
    /*
     * Merge walk througt edges from both intervals sets (this and given) and calls merge function 
     * implementation for each of them
     */
    private void merge(IntervalsSet second, MergeFunction operation) {
        Iterator<Entry<LocalDateTime, Boolean>> this_iterator = this.setMap.entrySet().iterator(); 
        Iterator<Entry<LocalDateTime, Boolean>> second_iterator = second.setMap.entrySet().iterator(); 
        
        TreeMap<LocalDateTime, Boolean> result = new TreeMap<>();
        boolean lastResultState = false;
       
        // get first steps
        Entry<LocalDateTime, Boolean> this_edge = this_iterator.hasNext() ? this_iterator.next() : null;
        Entry<LocalDateTime, Boolean> second_edge = second_iterator.hasNext() ? second_iterator.next() : null;
 
        while(this_edge != null || second_edge != null) {
            // intervals are here considered as closed-open - so if edge start, is inside
            // and if edge is end, it is outside.
            boolean this_state;
            boolean second_state;
            LocalDateTime step_time;
            
            // walk first this until is before second or second has no more edges
            if (this_edge != null && (second_edge == null || this_edge.getKey().isBefore(second_edge.getKey()))) {
                // in this, we are on the edge - if out-in edge, we are in, if in-out, we are out
                this_state = this_edge.getValue();
                step_time = this_edge.getKey();
                
                // in second, we are somewhere between edges, so state is determined by first next edge:
                second_state = second_edge == null ? false : !second_edge.getValue();
                
                this_edge = this_iterator.hasNext() ? this_iterator.next() : null;
            // else walk on second
            } else if (second_edge != null && (this_edge == null || second_edge.getKey().isBefore(this_edge.getKey()))) {
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
        
        this.setMap = result;
    }    
}
