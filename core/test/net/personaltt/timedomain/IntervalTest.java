/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import net.personaltt.timedomain.Interval;
import net.personaltt.timedomain.IntervalsSet;
import java.util.List;
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
public class IntervalTest {
    
    public IntervalTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    // all mutual positions of instance and other interval
    Interval instance;
    Interval beforeBefore;
    Interval beforeIn;
    Interval beforeAfter;
    Interval inIn;
    Interval inAfter;
    Interval afterAfter;
    
    @Before
    public void setUp() {
        instance = new Interval(new LocalDateTime(2010,1,5,0,0,0), new LocalDateTime(2010,1,10,0,0,0));
        beforeBefore = new Interval(new LocalDateTime(2010,1,1,0,0,0), new LocalDateTime(2010,1,4,0,0,0));
        
        beforeIn = new Interval(new LocalDateTime(2010,1,1,0,0,0), new LocalDateTime(2010,1,7,0,0,0));
        beforeAfter = new Interval(new LocalDateTime(2010,1,1,0,0,0), new LocalDateTime(2010,1,14,0,0,0));
        inIn = new Interval(new LocalDateTime(2010,1,6,0,0,0), new LocalDateTime(2010,1,8,0,0,0));
        inAfter = new Interval(new LocalDateTime(2010,1,6,0,0,0), new LocalDateTime(2010,1,14,0,0,0));
        afterAfter = new Interval(new LocalDateTime(2010,1,12,0,0,0), new LocalDateTime(2010,1,14,0,0,0));
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of toIntervalsSet method, of class Interval.
     */
    @Test
    public void testToIntervalsSet() {
        System.out.println("toIntervalsSet");
        
        Interval instance = new Interval(new LocalDateTime(2010,1,1,10,0,0), new LocalDateTime(2010,1,1,23,0,0));
        
        IntervalsSet result = instance.toIntervalsSet();
        
        List<Interval> intervalsOfIntervalSet = result.getIntervals();
        
        assertEquals(1, intervalsOfIntervalSet.size());
        
        assertEquals(instance, intervalsOfIntervalSet.get(0));
       
    }

    /**
     * Test of intersects method, of class Interval.
     */
    @Test
    public void testIntersectsBeforeBefore() {
        System.out.println("intersects");
        
        assertFalse(instance.intersects(beforeBefore));        
    }
    /**
     * Test of intersects method, of class Interval.
     */
    @Test
    public void testIntersectsBeforeIn() {
        System.out.println("intersects");
        assertTrue(instance.intersects(beforeIn));        
    }
    /**
     * Test of intersects method, of class Interval.
     */
    @Test
    public void testIntersectsBeforeAfter() {
        System.out.println("intersects");
        assertTrue(instance.intersects(beforeAfter));        
    }
    /**
     * Test of intersects method, of class Interval.
     */
    @Test
    public void testIntersectsInIn() {
        System.out.println("intersects");
        assertTrue(instance.intersects(inIn));        
    }
    /**
     * Test of intersects method, of class Interval.
     */
    @Test
    public void testIntersectsInAfter() {
        System.out.println("intersects");
        assertTrue(instance.intersects(inAfter));        
    }
    /**
     * Test of intersects method, of class Interval.
     */
    @Test
    public void testIntersectsAfterAfter() {
        System.out.println("intersects");
        assertFalse(instance.intersects(afterAfter));        
    }

    
     /**
     * Test of intersects method, of class Interval.
     */
    @Test
    public void testOverlapsBeforeBefore() {
        System.out.println("overlaps");
        
        assertFalse(instance.overlaps(beforeBefore));        
    }
    /**
     * Test of overlaps method, of class Interval.
     */
    @Test
    public void testOverlapsBeforeIn() {
        System.out.println("overlaps");
        assertFalse(instance.overlaps(beforeIn));        
    }
    /**
     * Test of overlaps method, of class Interval.
     */
    @Test
    public void testOverlapsBeforeAfter() {
        System.out.println("overlaps");
        assertFalse(instance.overlaps(beforeAfter));        
    }
    /**
     * Test of overlaps method, of class Interval.
     */
    @Test
    public void testOverlapsInIn() {
        System.out.println("overlaps");
        assertTrue(instance.overlaps(inIn));        
    }
    /**
     * Test of overlaps method, of class Interval.
     */
    @Test
    public void testOverlapsInAfter() {
        System.out.println("overlaps");
        assertFalse(instance.overlaps(inAfter));        
    }
    /**
     * Test of overlaps method, of class Interval.
     */
    @Test
    public void testOverlapsAfterAfter() {
        System.out.println("overlaps");
        assertFalse(instance.overlaps(afterAfter));        
    }
}
