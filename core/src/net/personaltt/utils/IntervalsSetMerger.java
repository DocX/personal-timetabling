/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Intervals set merger provides method and interface for merging two sets of 
 * intervals represented by intervals stops lists
 * @author docx
 * @param  <T> Type of intervals stops
 * @param  <V> Type of interval values
 */
public class IntervalsSetMerger<T extends Comparable,V1,V2> {
    
    Iterator<Map.Entry<T, V1>> this_iterator;
    Iterator<Map.Entry<T, V2>> second_iterator;

    public IntervalsSetMerger(Iterator<Entry<T, V1>> this_iterator, Iterator<Entry<T, V2>> second_iterator) {
        this.this_iterator = this_iterator;
        this.second_iterator = second_iterator;
    }
    
    /*
     * Merge walk througt edges from both intervals sets (this and given) and calls merge function 
     * implementation for each of them
     */
    public <R> TreeMap<T, R> merge(MergeFunction<R, V1, V2> operation) {
        
        TreeMap<T, R> result = new TreeMap<>();
        R lastResultState = null;
       
        // get first steps
        Map.Entry<T, V1> this_edge = this_iterator.hasNext() ? this_iterator.next() : null;
        Map.Entry<T, V2> second_edge = second_iterator.hasNext() ? second_iterator.next() : null;
        
        Map.Entry<T, V1> this_before = null;
        Map.Entry<T, V2> second_before = null;
 
        while(this_edge != null || second_edge != null) {
            // intervals are here considered as closed-open - so if edge start, is inside
            // and if edge is end, it is outside.
            V1 this_state;
            V2 second_state;
            T step_time;
            
            // walk first this until is before second or second has no more edges
            if (this_edge != null && (second_edge == null || this_edge.getKey().compareTo(second_edge.getKey()) < 0)) {
                // in this, we are on the edge - if out-in edge, we are in, if in-out, we are out
                this_state = this_edge.getValue();
                step_time = this_edge.getKey();
                
                // in second_edge we have edge greater than this_edge, so current state 
                // of second set is state of first edge before second_edge
                second_state = second_before == null ? null : second_before.getValue();
                
                this_before = this_edge;
                this_edge = this_iterator.hasNext() ? this_iterator.next() : null;
            // else walk on second
            } else if (second_edge != null && (this_edge == null || second_edge.getKey().compareTo(this_edge.getKey()) < 0 )) {
                // in second, we are on the edge - if out-in edge, we are in, if in-out, we are out
                second_state = second_edge.getValue();
                step_time = second_edge.getKey();
                
                // in this, we are somewhere between edges, so state is determined by first next edge:
                this_state = this_before == null ? null : this_before.getValue();
                
                second_before = second_edge;
                second_edge = second_iterator.hasNext() ? second_iterator.next() : null;
            // else must be equal not null
            } else {
                this_state = this_edge.getValue();
                second_state = second_edge.getValue();
                step_time = this_edge.getKey();
                
                // move both
                this_before = this_edge;
                this_edge = this_iterator.hasNext() ? this_iterator.next() : null;
                second_before = second_edge;
                second_edge = second_iterator.hasNext() ? second_iterator.next() : null;
            }
            
            // if result of merging current state on the edge is other than current state 
            // in the result interval set, add switch edge
            R result_state_of_edge = operation.mergeEdge(lastResultState, this_state, second_state);
            if (result_state_of_edge != null) {
                result.put(step_time, result_state_of_edge);
                lastResultState = result_state_of_edge;
            }
        }
        
        return result;
    }    
    
    public interface MergeFunction<VR,V1,V2> {    
        /*
         * Determine state of resulting interval set on the edge in given states of two intervals sets
         * Return null if no edge is added.
         */
        VR mergeEdge(VR previous_state, V1 state_a, V2 state_b);
    }   
    
}
