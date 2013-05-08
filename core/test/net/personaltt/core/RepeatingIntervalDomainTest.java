/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.core;

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
public class RepeatingIntervalDomainTest {
    
    public RepeatingIntervalDomainTest() {
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
     * Test of getIntervalsIn method, of class RepeatingIntervalDomain.
     */
    @Test
    public void testGetIntervalsInDayForDays() {
        System.out.println("getIntervalsIn 2 days each 7 days");
        Interval i = new Interval(
                new LocalDateTime(2010,1,6,0,0,0), 
                new LocalDateTime(2010,3,20,0,0,0));
        RepeatingIntervalDomain instance = new RepeatingIntervalDomain(
                new LocalDateTime(2010,1,12,0,0,0),
                Days.TWO, 
                Days.SEVEN
                );
        IntervalsSet result = instance.getIntervalsIn(i);
        
        // check all intervals
        Iterable<Interval> intervals = result.getIntervals();
        for (Interval interval : intervals) {
            int days = Days.daysBetween(interval.start, interval.end).getDays();
            assertTrue(days == 2);
            assertTrue(interval.intersects(i));
        }
        
        
        
    }
    
    /**
     * Test of getIntervalsIn method, of class RepeatingIntervalDomain.
     */
    @Test
    public void testGetIntervalsInRange1() {
        Interval i = new Interval(
                new LocalDateTime(2009,1,6,0,0,0), 
                new LocalDateTime(2010,5,20,0,0,0));
        RepeatingIntervalDomain instance = new RepeatingIntervalDomain(
                new LocalDateTime(2010,1,12,0,0,0),
                Days.TWO, 
                Months.ONE
                );
        IntervalsSet result = instance.getIntervalsIn(i);
        
        // check all intervals
        Iterable<Interval> intervals = result.getIntervals();        
        for (Interval interval : intervals) {
            int days = Days.daysBetween(interval.start, interval.end).getDays();
            assertEquals(days, 2);
            assertEquals(interval.start.getDayOfMonth(), 12);
            assertTrue(interval.intersects(i));
        }
        
    }
    /**
     * Test of getIntervalsIn method, of class RepeatingIntervalDomain.
     */
    @Test
    public void testGetIntervalsInRange2() {
        Interval i = new Interval(
                new LocalDateTime(2009,1,6,0,0,0), 
                new LocalDateTime(2010,10,20,0,0,0));
        RepeatingIntervalDomain instance = new RepeatingIntervalDomain(
                new LocalDateTime(2010,1,12,9,0,0),
                Hours.hours(10), 
                Months.ONE
                );
        IntervalsSet result = instance.getIntervalsIn(i);
        
        // check all intervals
        Iterable<Interval> intervals = result.getIntervals();        
        for (Interval interval : intervals) {
            int hours = Hours.hoursBetween(interval.start, interval.end).getHours();
            assertEquals(hours, 10);
            assertEquals(interval.start.getDayOfMonth(), 12);
            assertEquals(interval.start.getHourOfDay(), 9);
            assertTrue(interval.intersects(i));
        }

        
    }
}
