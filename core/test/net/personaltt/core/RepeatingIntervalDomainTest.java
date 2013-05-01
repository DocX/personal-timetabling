/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.core;

import org.joda.time.Days;
import org.joda.time.LocalDateTime;
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
        Interval i = 
                new Interval(new LocalDateTime(2010,1,6,0,0,0), new LocalDateTime(2010,1,20,0,0,0));
        RepeatingIntervalDomain instance = new RepeatingIntervalDomain(
                new LocalDateTime(2010,1,12,0,0,0),
                Days.TWO,
                Days.SEVEN
                );
        IntervalsSet result = instance.getIntervalsIn(i);
        
        // check all intervals
        for (Interval interval : result.getIntervals()) {
            int days = Days.daysBetween(interval.start, interval.end).getDays();
            assertTrue(days == 2);
            assertTrue(i.start.isBefore(interval.start) || i.start.isEqual(interval.start));
            assertTrue(i.end.isAfter(interval.end) || i.end.isEqual(interval.end));
        }
        
    }
}
