/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.test;

import java.util.List;
import net.personaltt.core.IntersectionDomain;
import net.personaltt.core.Interval;
import net.personaltt.core.IntervalsSet;
import net.personaltt.core.RepeatingIntervalDomain;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;

/**
 *
 * @author docx
 */
public class TestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RepeatingIntervalDomain weekends = new RepeatingIntervalDomain(
                new LocalDateTime(2013,1,5,0,0,0),
                Days.TWO,
                Days.SEVEN
                );
        
        RepeatingIntervalDomain workHours = new RepeatingIntervalDomain(
                new LocalDateTime(2013,1,1,9,0,0),
                Hours.EIGHT,
                Days.ONE
                );
        
        IntersectionDomain workHoursWeekends = new IntersectionDomain(workHours, weekends);
        
        for (int i = 0; i < 1; i++) {
            IntervalsSet s = workHoursWeekends.getIntervalsIn(new Interval(new LocalDateTime(2010,12,28,0,0,0), new LocalDateTime(2012,2,1,0,0,0)));

            List<Interval> intervals = s.getIntervals();
            for (Interval interval : intervals) {
                System.out.println(interval);
            }
            
            
        }
                
    }
}
