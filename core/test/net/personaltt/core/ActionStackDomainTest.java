/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.core;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author docx
 */
public class ActionStackDomainTest {
    
    public ActionStackDomainTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getIntervalsIn method, of class ActionStackDomain.
     */
    @Test
    public void testGetIntervalsIn1() {
        System.out.println("getIntervalsIn add");
        
        ActionStackDomain d = new ActionStackDomain();
        d.push(ActionStackDomain.ADD, new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0)));
        d.push(ActionStackDomain.ADD, new Interval(new LocalDateTime(2000,1,3,0,0,0), new LocalDateTime(2000,1,4,0,0,0)));
        
        ArrayList<Interval> expected = new ArrayList<>();
        expected.add(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0)));
        expected.add(new Interval(new LocalDateTime(2000,1,3,0,0,0), new LocalDateTime(2000,1,4,0,0,0)));

        IntervalsSet intervalset = d.getIntervalsIn(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,5,0,0,0)));
        List<Interval> intervals = intervalset.getIntervals();
        
        assertArrayEquals(
                expected.toArray(),
                intervals.toArray()
                );
        
    }
    
     @Test
    public void testGetIntervalsIn3() {
        System.out.println("getIntervalsIn add");
        
        ActionStackDomain d = new ActionStackDomain();
        d.push(ActionStackDomain.ADD, new RepeatingIntervalDomain(new LocalDateTime(2008,01,05,0,0,0), Days.TWO,Days.SEVEN));
        

        IntervalsSet intervalset = d.getIntervalsIn(
                new Interval(
                    new LocalDateTime(DateTime.now()).minus(Days.days(10)), 
                    new LocalDateTime(DateTime.now()).plus(Days.days(10))
                )
                );
        List<Interval> intervals = intervalset.getIntervals();

        // just dont throw anythink :]
    }

    
    @Test
    public void testGetIntervalsIn2() {
        System.out.println("getIntervalsIn add remove");
        
        ActionStackDomain d = new ActionStackDomain();
        d.push(ActionStackDomain.ADD, new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,3,0,0,0)));
        d.push(ActionStackDomain.REMOVE, new Interval(new LocalDateTime(2000,1,2,0,0,0), new LocalDateTime(2000,1,4,0,0,0)));
        
        ArrayList<Interval> expected = new ArrayList<>();
        expected.add(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0)));

        IntervalsSet intervalset = d.getIntervalsIn(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,5,0,0,0)));
        List<Interval> intervals = intervalset.getIntervals();
        
        assertArrayEquals(
                expected.toArray(),
                intervals.toArray()
                );
        
    }
    
    @Test
    public void testGetIntervalsInRepeatingAddThenMask() {
        System.out.println("getIntervalsIn add+mask repeating");
        
        ActionStackDomain d = new ActionStackDomain();
        d.push(ActionStackDomain.ADD, 
                new RepeatingIntervalDomain(new LocalDateTime(2010,1,1,0,0,0), Days.TWO, Days.SEVEN));
        d.push(ActionStackDomain.MASK,
                new RepeatingIntervalDomain(new LocalDateTime(2010,1,1,9,0,0), Hours.hours(10), Days.ONE));
        
        Interval i = new Interval(
                new LocalDateTime(2010,1,1,0,0,0),
                new LocalDateTime(2010,2,1,0,0,0));
        IntervalsSet intervalset = d.getIntervalsIn(i);
        List<Interval> intervals = intervalset.getIntervals();
        
        // there are 5 weeks in january 2010
        assertEquals(10, intervals.size());
        
        // check all intervals
        for (Interval interval : intervals) {
            // must be 10 hours long
            int hours = Hours.hoursBetween(interval.start, interval.end).getHours();
            assertEquals(10, hours);
            
            // starts at 9
            assertEquals(9, interval.start.getHourOfDay());
            
            // same day
            assertEquals(interval.start.getDayOfYear(), interval.end.getDayOfYear());
            assertEquals(interval.start.getYear(), interval.end.getYear());
            
            // is friday or sonday
            assertTrue(interval.start.getDayOfWeek() == 5 || interval.start.getDayOfWeek()==6);
            
            // is in wanted range
            assertTrue(interval.intersects(i));
        }
        
    }
    
     public void testGetIntervalsRepeatingAddThenRemove() {
        System.out.println("getIntervalsIn add remove");
        
        ActionStackDomain d = new ActionStackDomain();
        d.push(ActionStackDomain.ADD, 
                new RepeatingIntervalDomain(new LocalDateTime(2010,1,1,0,0,0), Days.THREE, Months.ONE));
        d.push(ActionStackDomain.REMOVE,
                new RepeatingIntervalDomain(new LocalDateTime(2010,1,1,0,0,0), Days.ONE, Days.SEVEN));
        
        Interval range = new Interval(
                new LocalDateTime(2010,1,1,0,0,0), 
                new LocalDateTime(2010,5,30,0,0,0)
            );
        
        IntervalsSet intervalset = d.getIntervalsIn(range);
        List<Interval> intervals = intervalset.getIntervals();
        
        ArrayList<Interval> expecteds = new ArrayList<>();
        expecteds.add(new Interval(new LocalDateTime(2010,1,2,0,0,0), new LocalDateTime(2010,1,4,0,0,0)));
        expecteds.add(new Interval(new LocalDateTime(2010,2,1,0,0,0), new LocalDateTime(2010,2,4,0,0,0)));
        expecteds.add(new Interval(new LocalDateTime(2010,3,1,0,0,0), new LocalDateTime(2010,3,4,0,0,0)));
        expecteds.add(new Interval(new LocalDateTime(2010,4,1,0,0,0), new LocalDateTime(2010,4,2,0,0,0)));
        expecteds.add(new Interval(new LocalDateTime(2010,4,3,0,0,0), new LocalDateTime(2010,4,4,0,0,0)));
        expecteds.add(new Interval(new LocalDateTime(2010,5,1,0,0,0), new LocalDateTime(2010,5,4,0,0,0)));
      
        assertArrayEquals(expecteds.toArray(), intervals.toArray());
        
    }
    
   
}
