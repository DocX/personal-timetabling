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
import net.personaltt.model.Occurrence;

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
    
    public static <K extends Comparable> BaseIntervalsSet<K> oneInterval(K start, K end) {
        BaseIntervalsSet<K> set = new BaseIntervalsSet<>();
        set.unionWith(start, end);
        return set;
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
            return new BaseInterval<>(this.setMap.floorEntry(start).getKey(), this.setMap.higherKey(start));
        } else {
            return null;
        }
    } 

    public BaseInterval<T> getFirstInterval() {
        if (this.setMap.isEmpty()) {
            return null;
        }
        
        T first = this.setMap.firstKey();
        return new BaseInterval<>(first, this.setMap.higherKey(first));
    }
    
    /**
     * Return start of first interval in set according to natural ordering of keys.
     * @return 
     */
    public T getUpperBound() {
        return setMap.lastKey();
    }

    /**
     * Return end of last interval in set according to natural ordering of keys;
     * @return 
     */
    public T getLowerBound() {
        return setMap.firstKey();
    }
    
   /**
     * Union merge operation
     */
    private class UnionMerge implements IntervalsSetMerger.MergeFunction<Boolean, Boolean, Boolean> {
        
        @Override
        public Boolean mergeEdge(Boolean prev, Boolean state_a, Boolean state_b) {
            boolean new_state = 
                    (state_a == null ? false : state_a.booleanValue()) || 
                    (state_b == null ? false : state_b.booleanValue());
            return new_state == (prev == null ? false : prev.booleanValue()) ? null : new_state;
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
    private class IntersectMerge implements IntervalsSetMerger.MergeFunction<Boolean, Boolean, Boolean> {
        @Override
        public Boolean mergeEdge(Boolean prev, Boolean state_a, Boolean state_b) {
            boolean new_state = 
                    (state_a == null ? false : state_a.booleanValue()) && 
                    (state_b == null ? false : state_b.booleanValue());
            return new_state == (prev == null ? false : prev.booleanValue()) ? null : new_state;
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
    
    public BaseIntervalsSet<T> getIntersectionWith(BaseIntervalsSet<T> set) {
        BaseIntervalsSet<T> result = new BaseIntervalsSet<>();
        result.setMap = this.getMerged(set.setMap.entrySet().iterator(), new IntersectMerge());
        return result;
    }
   
    
    /**
    * Minus merge operation
    */
    private class MinusMerge implements IntervalsSetMerger.MergeFunction<Boolean, Boolean, Boolean> {
        @Override
        public Boolean mergeEdge(Boolean prev, Boolean state_a, Boolean state_b) {
            boolean new_state = 
                    (state_a == null ? false : state_a.booleanValue()) && 
                    !(state_b == null ? false : state_b.booleanValue());
            return new_state == (prev == null ? false : prev.booleanValue()) ? null : new_state;
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
    private void merge(BaseIntervalsSet<T> second, IntervalsSetMerger.MergeFunction<Boolean, Boolean,Boolean> operation) {
        this.setMap = this.getMerged(second.setMap.entrySet().iterator(), operation);
    }
    
    private TreeMap<T, Boolean> getMerged(Iterator<Map.Entry<T, Boolean>> second_iterator, IntervalsSetMerger.MergeFunction<Boolean, Boolean,Boolean> operation) {
        return new IntervalsSetMerger<>(this.setMap.entrySet().iterator(), second_iterator)
                .merge(operation);
    }
    
    /**
     * Converts this intervals set to the same set represented with keys converted
     * from keys of this using given converter. 
     * @param <D> Type parametr of target keys
     * @param converter Converter object used for transform keys to new set
     * @return 
     */
    public <D extends Comparable> BaseIntervalsSet<D> convertTo(Converter<T,D> converter){
        BaseIntervalsSet<D> target = new BaseIntervalsSet<>();
        
        for (Entry<T, Boolean> entry : this.setMap.entrySet()) {
            target.setMap.put(converter.convert(entry.getKey()), entry.getValue());
        }
        
        return target;
    }
    
    public boolean empty() {
        return setMap.isEmpty();
    }
}
