/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import net.personaltt.model.Occurrence;
import net.personaltt.solver.heuristics.StopsAlignedAllocationsEnumerator;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.Iterables;
import net.personaltt.utils.ValuedInterval;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;
import net.personaltt.utils.intervalmultimap.IntervalMultimap.MultimapEdge;


/**
 * Occurrence allocations iterator. Provides method for enumerating occurrences
 * allocations that are reasonable in terms of cost computation.
 * @author docx
 */
public class AlignedOccurrenceAllocationsIterator implements Iterator<BaseInterval<Integer>> {  
    
    Occurrence occurrence;
    
    IntervalMultimap<Occurrence> schedule;
    
    ListIterator<BaseInterval<Integer>> occurrenceDomainIntervals;
    
    StopsAlignedAllocationsEnumerator allocationsEnum;

    public AlignedOccurrenceAllocationsIterator(Occurrence occurrence, IntervalMultimap<Occurrence> schedule) {
        this.occurrence = occurrence;
        this.schedule = schedule;
        
        occurrenceDomainIntervals  = occurrence.getDomain().getBaseIntervals().listIterator();
    }

    @Override
    public boolean hasNext() {
        return occurrenceDomainIntervals.hasNext() || allocationsEnum.hasNext();
    }

    @Override
    public BaseInterval<Integer> next() {
        if (allocationsEnum == null || !allocationsEnum.hasNext()) {
            BaseInterval<Integer> nextDomain = occurrenceDomainIntervals.next();
          
            // get elementary intervals in continuous domain interval
            ArrayList<ValuedInterval<Integer, MultimapEdge<Occurrence>>> elementaryIntervals = 
                    Iterables.toArrayList(schedule.valuesChangesInInterval(nextDomain));
            
            // enumerator of allocations from minimal to maximal duration, that are aligned to elementary intervals starts
            allocationsEnum = 
                    new StopsAlignedAllocationsEnumerator(occurrence.getMinDuration(), occurrence.getMaxDuration(), elementaryIntervals);
        }
        
        StopsAlignedAllocationsEnumerator.AlignedAllocation allocation = allocationsEnum.next();
        return new BaseInterval<>(allocation.startPoint, allocation.endPoint);       
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Skips to next available allocation start and thus skipping all durations left on current start. 
     */
    public void skipDurationIncreasing() {
        allocationsEnum.skipToNextStop();
    }
    
}
