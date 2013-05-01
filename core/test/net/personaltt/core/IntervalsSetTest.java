/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.core;

import java.util.ArrayList;
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
public class IntervalsSetTest {
    
    public IntervalsSetTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    IntervalsSet set1;
    IntervalsSet set2;
    
    @Before
    public void setUp() {
        // set up set of sets
        set1 = new IntervalsSet();
        set1.unionWith(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0));
        set1.unionWith(new LocalDateTime(2000,1,3,0,0,0), new LocalDateTime(2000,1,4,0,0,0));
        set1.unionWith(new LocalDateTime(2000,1,5,0,0,0), new LocalDateTime(2000,1,6,0,0,0));
        set1.unionWith(new LocalDateTime(2000,1,7,0,0,0), new LocalDateTime(2000,1,8,0,0,0));
        
        set2 = new IntervalsSet();
        set2.unionWith(new LocalDateTime(2000,1,3,0,0,0), new LocalDateTime(2000,1,6,0,0,0));
        set2.unionWith(new LocalDateTime(2000,1,9,0,0,0), new LocalDateTime(2000,1,10,0,0,0));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of unionWith method, of class IntervalsSet.
     */
    @Test
    public void testUnionWith_IntervalsSet1() {
        System.out.println("unionWith empty with intervalset ");
        IntervalsSet instance = new IntervalsSet();
        instance.unionWith(set1);
        
        assertArrayEquals(instance.getIntervals().toArray(), set1.getIntervals().toArray());
    }

    /**
     * Test of unionWith method, of class IntervalsSet.
     */
    @Test
    public void testUnionWith_IntervalsSet2() {
        System.out.println("unionWith intervalset ");
        IntervalsSet instance = new IntervalsSet();
        instance.unionWith(set1);
        instance.unionWith(set2);
        
        ArrayList<Interval> expected = new ArrayList<>();
        expected.add(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0)));
        expected.add(new Interval(new LocalDateTime(2000,1,3,0,0,0), new LocalDateTime(2000,1,6,0,0,0)));
        expected.add(new Interval(new LocalDateTime(2000,1,7,0,0,0), new LocalDateTime(2000,1,8,0,0,0)));
        expected.add(new Interval(new LocalDateTime(2000,1,9,0,0,0), new LocalDateTime(2000,1,10,0,0,0)));
        
        assertArrayEquals(instance.getIntervals().toArray(), expected.toArray());
    }

   
    /**
     * Test of unionWith method, of class IntervalsSet.
     */
    @Test
    public void testUnionWith_LocalDateTime_LocalDateTime1() {
        System.out.println("unionWith new with one localdate");
        Interval i = new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0));

        IntervalsSet set = new IntervalsSet();
        set.unionWith(i.start, i.end);
        
        List<Interval> intervals = set.getIntervals();
        assertTrue(intervals.size() == 1);
        assertEquals(intervals.get(0), i);
    }

    @Test
    public void testUnionWith_LocalDateTime_LocalDateTime_Distinct() {
        System.out.println("unionWith new with few distinct localdate");
        ArrayList<Interval> is = new ArrayList<>();
        is.add(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0)));
        is.add(new Interval(new LocalDateTime(2000,1,10,0,0,0), new LocalDateTime(2000,1,12,0,0,0)));
        is.add(new Interval(new LocalDateTime(2000,1,15,0,0,0), new LocalDateTime(2000,1,16,0,0,0)));
        is.add(new Interval(new LocalDateTime(2000,1,20,0,0,0), new LocalDateTime(2000,2,3,0,0,0)));

        IntervalsSet set = new IntervalsSet();
        for (Interval interval : is) {
            set.unionWith(interval.start, interval.end);
        }
        
        List<Interval> intervals = set.getIntervals();
        assertTrue(intervals.size() == is.size());
        assertArrayEquals(is.toArray(), intervals.toArray());
    }
    
    @Test
    public void testUnionWith_LocalDateTime_LocalDateTime_Seamless() {
        System.out.println("unionWith new with few touching localdate");
        ArrayList<Interval> is = new ArrayList<>();
        is.add(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0)));
        is.add(new Interval(new LocalDateTime(2000,1,2,0,0,0), new LocalDateTime(2000,1,12,0,0,0)));
        is.add(new Interval(new LocalDateTime(2000,1,15,0,0,0), new LocalDateTime(2000,1,16,0,0,0)));
        is.add(new Interval(new LocalDateTime(2000,1,16,0,0,0), new LocalDateTime(2000,2,3,0,0,0)));

        IntervalsSet set = new IntervalsSet();
        for (Interval interval : is) {
            set.unionWith(interval.start, interval.end);
        }

        ArrayList<Interval> expected = new ArrayList<>();
        expected.add(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,12,0,0,0)));
        expected.add(new Interval(new LocalDateTime(2000,1,15,0,0,0), new LocalDateTime(2000,2,3,0,0,0)));
        
        
        List<Interval> intervals = set.getIntervals();
        assertTrue(intervals.size() == expected.size());
        assertArrayEquals(expected.toArray(), intervals.toArray());
    }

    /**
     * Test of intersectWith method, of class IntervalsSet.
     */
    @Test
    public void testIntersectWith() {
        System.out.println("intersectWith");
        IntervalsSet instance = new IntervalsSet();
        instance.unionWith(set1);
        instance.intersectWith(set2);
        
        ArrayList<Interval> expected = new ArrayList<>();
        expected.add(new Interval(new LocalDateTime(2000,1,3,0,0,0), new LocalDateTime(2000,1,4,0,0,0)));
        expected.add(new Interval(new LocalDateTime(2000,1,5,0,0,0), new LocalDateTime(2000,1,6,0,0,0)));
        
        assertArrayEquals(expected.toArray(), instance.getIntervals().toArray());
    }

    /**
     * Test of minus method, of class IntervalsSet.
     */
    @Test
    public void testMinus() {
        System.out.println("minus");
        IntervalsSet instance = new IntervalsSet();
        instance.unionWith(set1);
        instance.minus(set2);
        
        ArrayList<Interval> expected = new ArrayList<>();
        expected.add(new Interval(new LocalDateTime(2000,1,1,0,0,0), new LocalDateTime(2000,1,2,0,0,0)));
        expected.add(new Interval(new LocalDateTime(2000,1,7,0,0,0), new LocalDateTime(2000,1,8,0,0,0)));
        
        assertArrayEquals(expected.toArray(), instance.getIntervals().toArray());
    }

    
}