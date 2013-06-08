/* Intervals set. Represents set of intervals and implements set operations on it
 */
package net.personaltt.timedomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.personaltt.utils.BaseIntervalsSet;
import net.personaltt.utils.Converter;
import org.joda.time.LocalDateTime;

/**
 * Extension of Intervals set of intervals on LocalDateTime. Adds some syntactic suger
 * methods.
 * @author docx
 */
public class IntervalsSet extends BaseIntervalsSet<LocalDateTime> {

    /**
     * Creates intervals set with given one interval
     * @param interval 
     */
    public IntervalsSet(Interval interval) {
        super();
        setMap.put(interval.getStart(), Boolean.TRUE);
        setMap.put(interval.getEnd(), Boolean.FALSE);
    }

    public IntervalsSet() {
        super();
    }
    
    /**
     * Union with single interval
     * @param interval 
     */
    public void unionWith(Interval interval) {
        this.unionWith(interval.getStart(), interval.getEnd());
    }    
  
    
    /**
     * Minux single interval
     * @param subtrahend 
     */
    public void minus(Interval subtrahend){
        IntervalsSet s = new IntervalsSet(subtrahend);
        this.minus(s);
    }
    
    private class IntervalListBuilder implements IntervalsBuilder<LocalDateTime> {
        
        List<Interval> intervals;

        public IntervalListBuilder() {
            intervals = new ArrayList<>();
        }
        
        @Override
        public void add(LocalDateTime start, LocalDateTime end) {
            intervals.add(new Interval(start, end));
        }
        
    }
    
    public List<Interval> getIntervals() {
        IntervalListBuilder builder = new IntervalListBuilder();
        buildIntervals(builder);
        return builder.intervals;
    }
    
    /**
     * Converts LocalDateTime intervals set to numeric intervals
     * using seconds since Unix epoch of times.
     * @return 
     */
    public BaseIntervalsSet<Integer> toNumericIntervalsSet() {
       return this.convertTo(new ConverterToInteger());
    }
}
