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
    
    
    public void unionWith(IntervalsSet i) {
        // union with each interval
        for (Iterator<Entry<LocalDateTime, Boolean>> it = i.setMap.entrySet().iterator(); it.hasNext();) {
            Entry<LocalDateTime, Boolean> object = it.next();
            // start and has next
            if (object.getValue() && it.hasNext())  {
                Entry<LocalDateTime, Boolean> end = it.next();
                
                unionWith(object.getKey(), end.getKey());
            }
        }
    }
    void unionWith(Interval interval) {
        this.unionWith(interval.start, interval.end);
    }
    void unionWith(LocalDateTime start, LocalDateTime end) {
        
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
    
    
    public void intersectWith(IntervalsSet mask) {
        // cases:
        // this: |-----|  |---|   |-----|   |----|   
        // mask:   |--------|   |-----------|
        // rslt:   |---|  |-|     |-----|
        
        Iterator<Entry<LocalDateTime, Boolean>> it_this = this.setMap.entrySet().iterator(); 
        Iterator<Entry<LocalDateTime, Boolean>> it_mask = mask.setMap.entrySet().iterator(); 
        
        TreeMap<LocalDateTime, Boolean> intersection = new TreeMap<>();

        // get first steps
        boolean state_this = false;
        boolean state_mask = false;
        Entry<LocalDateTime, Boolean> cu_this = it_this.hasNext() ? it_this.next() : null;
        Entry<LocalDateTime, Boolean> cu_mask = it_mask.hasNext() ? it_mask.next() : null;
        
        while(cu_this != null && cu_mask != null) {
            Entry<LocalDateTime, Boolean> edge;
            boolean state_intersection = state_this && state_mask;
            
            if (cu_this.getKey().isBefore(cu_mask.getKey())) {
                edge = cu_this;
                state_this = edge.getValue();
                cu_this = it_this.hasNext() ? it_this.next() : null;
            } else {
                edge = cu_mask;
                state_mask = edge.getValue();
                cu_mask = it_mask.hasNext() ? it_mask.next() : null;
            }
            
            // now in state_* is current state of both sets just >after< the edge
            // in the state_intersection is AND state of sets just >before< the edge
            // and in edge is edge causing this stop
            
            // if is endging enge and is in intersection before
            // or is starting edge and is in intersection after and not before
            if ((edge.getValue() == false && state_intersection == true) || 
                    (edge.getValue() == true && !state_intersection && state_this && state_mask) ) {
                // add ending edge to intersection
                intersection.put(edge.getKey(), edge.getValue());
            }
        }
        
        // construct new intervals set from edges list
        this.setMap = intersection;
    }

    void minus(IntervalsSet subtrahend_set) {
        // cases:
        // this: |-----|  |---|   |-----|   |----|   
        // minu:   |--------|   |-----------|
        // rslt: |-|        |-|             |----|
        
        Iterator<Entry<LocalDateTime, Boolean>> it_this = this.setMap.entrySet().iterator(); 
        Iterator<Entry<LocalDateTime, Boolean>> it_mask = subtrahend_set.setMap.entrySet().iterator(); 
        
        TreeMap<LocalDateTime, Boolean> intersection = new TreeMap<>();

        // get first steps
        boolean state_this = false;
        boolean state_mask = false;
        Entry<LocalDateTime, Boolean> cu_this = it_this.hasNext() ? it_this.next() : null;
        Entry<LocalDateTime, Boolean> cu_mask = it_mask.hasNext() ? it_mask.next() : null;
        
        while(cu_this != null && cu_mask != null) {
            LocalDateTime edge_date; boolean edge_state;
            boolean state_before = state_this && !state_mask;
            
            if (cu_this.getKey().isBefore(cu_mask.getKey())) {
                edge_date = cu_this.getKey();
                edge_state = cu_this.getValue();
                state_this = edge_state;
                cu_this = it_this.hasNext() ? it_this.next() : null;
            } else {
                edge_date = cu_mask.getKey();
                edge_state = !cu_mask.getValue(); // minus is inverted intersection
                state_mask = cu_mask.getValue();
                cu_mask = it_mask.hasNext() ? it_mask.next() : null;
            }
            
            if ((edge_state == false && state_before == true) || 
                    (edge_state == true && !state_before && state_this && !state_mask) ) {
                // add ending edge to intersection
                intersection.put(edge_date, edge_state);
            }
            
        }
        
        // construct new intervals set from edges list
        this.setMap = intersection;
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
}
