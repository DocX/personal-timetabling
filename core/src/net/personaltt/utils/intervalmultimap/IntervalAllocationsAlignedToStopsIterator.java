/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils.intervalmultimap;

import java.util.Iterator;
import java.util.List;
import net.personaltt.model.Occurrence;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;
import net.personaltt.utils.Metric;

/**
 * Intervals aligned to stops iterator. Iterate over placements of interval 
 * defined by its length in M metric which are aligned (start or end) to stops
 * of given intervals list.
 * Iterates in ascending order.
 * @author docx
 */
public class IntervalAllocationsAlignedToStopsIterator<K extends Comparable, M> implements Iterator<IntervalAllocationsAlignedToStopsIterator<K,M>.IntervalStop> {
        
        M allocationDuration;
        List<? extends BaseInterval<K>> values;
        
        K startPoint;
        int startValuesIndex;
        K endPoint;
        int endValuesIndex;
        
        Metric<K,M> metric;
        
        public class IntervalStop {
            public K startPoint;
            public int startValuesIndex;
        }
        
        public IntervalAllocationsAlignedToStopsIterator(M allocationDuration, List<? extends BaseInterval<K>> values, Metric<K,M> metric) {
            
            this.metric = metric;
            this.allocationDuration = allocationDuration;
            this.values = values;

            startPoint = values.get(0).getStart();
            startValuesIndex = 0;
            
            endPoint = metric.add(startPoint, allocationDuration);
            endValuesIndex = 0;
            // find interval where is end point
            while(endValuesIndex < values.size() && values.get(endValuesIndex).getEnd().compareTo(endPoint) <= 0) { endValuesIndex++; }
        }
        
        @Override
        public boolean hasNext() {
            return endValuesIndex <= values.size();
        }

        @Override
        public IntervalStop next() {
            IntervalStop s = new IntervalStop();
            s.startPoint = startPoint;
            s.startValuesIndex = startValuesIndex;
            
            move();
            
            // return current
            return s;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private void move() {
            if(endValuesIndex >= values.size()) {
                endValuesIndex++;
                return;
            }
            
             // select next
            // move start to next stop of values change
            int compare = values.get(startValuesIndex).getEnd().compareTo(
                    metric.removes(values.get(endValuesIndex).getEnd(), allocationDuration)
                    );
            if (compare <= 0) {
                // next stop of start is closer to current start than next stop of end
                
                startPoint = values.get(startValuesIndex).getEnd();
                endPoint = metric.add(startPoint, allocationDuration);
                startValuesIndex++;
                
                if (compare == 0) {
                    endValuesIndex++;
                }
            } else {
                endPoint = values.get(endValuesIndex).getEnd() ;
                startPoint = metric.removes(endPoint, allocationDuration);
                endValuesIndex++;
            }
        }
    
    
}
