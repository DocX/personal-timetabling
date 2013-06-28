/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.List;
import net.personaltt.model.Occurrence;
import net.personaltt.timedomain.IntegerMetric;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;
import net.personaltt.utils.intervalmultimap.IntervalAllocationsAlignedToStopsIterator;

/**
 * Stops aligned allocations enumerator. Enumerates allocations, that
 * are aligned to edges of elementary intervals in some interval.
 * When elementary intervals are 
 * 
 * @author docx
 */
public class StopsAlignedAllocationsEnumerator {

    List<? extends BaseInterval<Integer>> elementaryIntervals;
    int minDuration, maxDuration;
    int intervalsUpperBound;
    
    IntervalAllocationsAlignedToStopsIterator<Integer, Integer> stopsIterator;
    int duration;
    IntervalAllocationsAlignedToStopsIterator<Integer,Integer>.IntervalStop alignment;
    int currentIntervalOfEnd;
    
    boolean nextIsSkipped = false;
            
    public StopsAlignedAllocationsEnumerator(int minDuration, int maxDuration, List<? extends BaseInterval<Integer>> intervals) {
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.elementaryIntervals = intervals;
        intervalsUpperBound = intervals.get(intervals.size() - 1).getEnd();
        
        stopsIterator = new IntervalAllocationsAlignedToStopsIterator<>(minDuration, intervals, new IntegerMetric());
    }
    
    public class AlignedAllocation {
        public int intervalIndexOfStartPoint;
        public int startPoint;
        public int endPoint;
    }
    
    public boolean hasNext() {
        return (alignment != null || stopsIterator.hasNext()) && 
                ((alignment != null && duration <= maxDuration && alignment.startPoint + duration <= intervalsUpperBound) || stopsIterator.hasNext());
    }
    
    public AlignedAllocation next() {
        // if duration is over, get next start stop
        if ((alignment != null && duration <= maxDuration && alignment.startPoint + duration <= intervalsUpperBound) == false) {
            this.nextStop();
        }
        
        AlignedAllocation ret = new AlignedAllocation();
        ret.startPoint = alignment.startPoint;
        ret.intervalIndexOfStartPoint = alignment.startValuesIndex;
        ret.endPoint = alignment.startPoint + duration;
        
        if (duration == maxDuration || alignment.startPoint + duration == intervalsUpperBound) {
            this.nextStop();
            nextIsSkipped = true;
            return ret;
        }
        
        //set next duration to min of duration to upper bound, max duration and next stop end 
        duration = Math.min(maxDuration, elementaryIntervals.get(currentIntervalOfEnd).getEnd() - alignment.startPoint);
        currentIntervalOfEnd++;
           
        return ret;
    }
    
    private void nextStop() {
        if (stopsIterator.hasNext() == false) {
            alignment = null;
            return;
        }
        
        duration = minDuration;
        alignment = stopsIterator.next();

        // find interval, where is end in
        currentIntervalOfEnd = alignment.startValuesIndex;
        while (currentIntervalOfEnd < elementaryIntervals.size() &&
                elementaryIntervals.get(currentIntervalOfEnd).getEnd() <= alignment.startPoint + duration) { currentIntervalOfEnd++; }
    }
    
    public void skipToNextStop() {
        if (nextIsSkipped) {
            nextIsSkipped = false;
            return;
        }
        
        this.nextStop();
    }
    
}
