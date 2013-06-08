/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.Iterator;
import java.util.List;
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
public class IntervalMultimapTest {
    
    public IntervalMultimapTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        IntervalMultimap<Integer,Object> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        
        // |----------------|
        // 10               50
        // 1                -
        
        assertEquals(2, instance.edges.size());
        assertNotNull(instance.edges.get(10));
        assertNotNull(instance.edges.get(50));
        assertEquals(1, instance.edges.get(10).size());
        assertEquals(1, instance.edges.get(10).get(0));
        assertEquals(0, instance.edges.get(50).size());
    }    
    
    /**
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testPut3() {
        System.out.println("put");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(20, 55));
        instance.put(3, new BaseInterval(10, 30));
        
        // |----|-----|-----|--|
        // 10   20    30    50 55
        // 1,3  1,2,3 1,2   2  -
        
        assertEquals(5, instance.edges.size());
        assertNotNull(instance.edges.get(10));
        assertNotNull(instance.edges.get(20));
        assertNotNull(instance.edges.get(30));
        assertNotNull(instance.edges.get(50));
        assertNotNull(instance.edges.get(55));
        
        assertArrayEquals(new Integer[] {1,3}, instance.edges.get(10).toArray());
        assertArrayEquals(new Integer[] {1,2,3}, instance.edges.get(20).toArray());
        assertArrayEquals(new Integer[] {1,2}, instance.edges.get(30).toArray());
        assertArrayEquals(new Integer[] {2}, instance.edges.get(50).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(55).toArray());
        
    }

    /**
     * Test of remove method, of class IntervalMultimap.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(20, 55));
        instance.put(3, new BaseInterval(10, 30));
        
        instance.remove(2);
        
        // |----|-----|-----|--|
        // 10   20    30    50 55
        // 1,3  1,3   1     -  /
        
        assertEquals(4, instance.edges.size());
        assertNotNull(instance.edges.get(10));
        assertNotNull(instance.edges.get(20));
        assertNotNull(instance.edges.get(30));
        assertNotNull(instance.edges.get(50));
        assertNull(instance.edges.get(55));
        
        assertArrayEquals(new Integer[] {1,3}, instance.edges.get(10).toArray());
        assertArrayEquals(new Integer[] {1,3}, instance.edges.get(20).toArray());
        assertArrayEquals(new Integer[] {1}, instance.edges.get(30).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(50).toArray());
        
    }

    /**
     * Test of remove method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveFirst() {
        System.out.println("remove");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(20, 55));
        instance.put(3, new BaseInterval(25, 30));

        // |----|---|-----|-----|--|
        // 10   20  25    30    50 55
        // 1    1,2 1,2,3 1,2   2  /

        instance.remove(1);
        // -----|---|-----|------|
        // 10   20  25    30     55
        // -    2   2,3   2      /
        
        
        assertEquals(4, instance.edges.size());
        assertNotNull(instance.edges.get(20));
        assertNotNull(instance.edges.get(25));
        assertNotNull(instance.edges.get(30));
        assertNotNull(instance.edges.get(55));
        assertNull(instance.edges.get(10));
        assertNull(instance.edges.get(50));
        
        assertArrayEquals(new Integer[] {2}, instance.edges.get(20).toArray());
        assertArrayEquals(new Integer[] {2,3}, instance.edges.get(25).toArray());
        assertArrayEquals(new Integer[] {2}, instance.edges.get(30).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(55).toArray());
        
    }
    
    
    /**
     * Test of valuesIn method, of class IntervalMultimap.
     */
    @Test
    public void testValuesIn() {
        System.out.println("valuesIn");
        IntervalMultimap instance = new IntervalMultimap();
        instance.put(10, new BaseInterval(10, 50));
        
        assertEquals(1, instance.valuesIn(15).size());
        assertEquals(10, instance.valuesIn(15).get(0));
    }

    /**
     * Test of valuesOfOverlappingIntervals method, of class IntervalMultimap.
     */
    @Test
    public void testValuesOfOverlappingIntervals() {
        System.out.println("valuesOfOverlappingIntervals");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(20, 55));
        instance.put(3, new BaseInterval(10, 30));
        instance.put(4, new BaseInterval(100, 130));
        instance.put(5, new BaseInterval(200, 230));
        instance.put(6, new BaseInterval(220, 250));
        instance.put(7, new BaseInterval(250, 270)); // not overlaps
        
        List<Integer> overlappingValues = instance.valuesOfOverlappingIntervals();
        
        assertEquals(5, overlappingValues.size());
        assertTrue(overlappingValues.contains(1));
        assertTrue(overlappingValues.contains(2));
        assertTrue(overlappingValues.contains(3));
        assertTrue(overlappingValues.contains(5));
        assertTrue(overlappingValues.contains(6));
        assertFalse(overlappingValues.contains(4));
        assertFalse(overlappingValues.contains(7));
        
    }
    
    
    /**
     * Test of valuesOfOverlappingIntervals method, of class IntervalMultimap.
     */
    @Test
    public void testValuesOfOverlappingIntervals2() {
        System.out.println("valuesOfOverlappingIntervals 2");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 25));
        instance.put(2, new BaseInterval(10, 25));
        instance.put(3, new BaseInterval(10, 25));
        
        List<Integer> overlappingValues = instance.valuesOfOverlappingIntervals();
        
        assertEquals(3, overlappingValues.size());
        assertTrue(overlappingValues.contains(1));
        assertTrue(overlappingValues.contains(2));
        assertTrue(overlappingValues.contains(3));
        
    }
    
    /**
     * Test of valuesOfOverlappingIntervals method, of class IntervalMultimap.
     */
    @Test
    public void testValuesOfOverlappingIntervalsWithDelete() {
        System.out.println("valuesOfOverlappingIntervals and Delete");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 25));
        instance.put(2, new BaseInterval(10, 25));
        instance.put(3, new BaseInterval(10, 25));
        
        instance.remove(2);
        
        List<Integer> overlappingValues = instance.valuesOfOverlappingIntervals();
        
        assertEquals(2, overlappingValues.size());
        assertTrue(overlappingValues.contains(1));
        assertTrue(overlappingValues.contains(3));
    }

    /**
     * Test of valuesOfOverlappingIntervals method, of class IntervalMultimap.
     */
    @Test
    public void testValuesOfOverlappingIntervalsWithDelete2() {
        System.out.println("valuesOfOverlappingIntervals and Delete");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 25));
        instance.put(2, new BaseInterval(10, 25));
        instance.put(3, new BaseInterval(10, 25));
        
        instance.remove(2);
        
        instance.put(2, new BaseInterval(10, 15));
        
        List<Integer> overlappingValues = instance.valuesOfOverlappingIntervals();
        
        assertEquals(3, overlappingValues.size());
        assertTrue(overlappingValues.contains(1));
        assertTrue(overlappingValues.contains(2));
        assertTrue(overlappingValues.contains(3));
    }    
    
    /**
     * Test of intervalsSetIterator method, of class IntervalMultimap.
     */
    @Test
    public void testIntervalsSetIterator() {

    }

    /**
     * Test of toIntervalsSet method, of class IntervalMultimap.
     */
    @Test
    public void testToIntervalsSet() {
        IntervalMultimap multimap = new IntervalMultimap();
        multimap.put(1, new BaseInterval(10,20));
        multimap.put(2, new BaseInterval(30,40));
        multimap.put(3, new BaseInterval(40,50));
        multimap.put(4, new BaseInterval(100,140));
        multimap.put(5, new BaseInterval(120,130));
        
        multimap.put(6, new BaseInterval(123,180));
        
        BaseInterval[] intervals = new BaseInterval[] {
            new BaseInterval(10,20),
            new BaseInterval(30,50),
            new BaseInterval(100,180)
        };
        
        Object[] actual = multimap.toIntervalsSet().getBaseIntervals().toArray();
        
        assertArrayEquals(intervals, actual);
    }
    
    /**
     * Test of toIntervalsSet method, of class IntervalMultimap.
     */
    @Test
    public void testGetIntervalsIn() {
        IntervalMultimap multimap = new IntervalMultimap();
        multimap.put(1, new BaseInterval(10,20));
        multimap.put(2, new BaseInterval(15,40));
        multimap.put(3, new BaseInterval(40,50));
        multimap.put(4, new BaseInterval(100,140));
        multimap.put(5, new BaseInterval(120,130));
        
        // 0   10  15  20  40  50  100 120 130 140 150
        // |---|---|---|---|---|---|---|---|---|---|
        // /   1  1,2  2   3   /   4   4,5 4   /   -
        
        BaseIntervalsSet domain = new BaseIntervalsSet();
        domain.unionWith(0, 150);
        
        List<IntervalMultimap.MultiInterval> actual = multimap.getIntervalsIn(domain);
        
        assertEquals(10, actual.size());
        int i = 0;
        
        assertEquals(actual.get(i).interval, new BaseInterval(0,10));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {});
        i++;
        
        assertEquals(actual.get(i).interval, new BaseInterval(10,15));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {1});
        i++;
        
        assertEquals(actual.get(i).interval, new BaseInterval(15,20));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {1,2});
        i++;
        
        assertEquals(actual.get(i).interval, new BaseInterval(20,40));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {2});
        i++;
        
        assertEquals(actual.get(i).interval, new BaseInterval(40,50));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {3});
        i++;
        
        assertEquals(actual.get(i).interval, new BaseInterval(50,100));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {});
        i++;
        
        assertEquals(actual.get(i).interval, new BaseInterval(100,120));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {4});
        i++;
        
        assertEquals(actual.get(i).interval, new BaseInterval(120,130));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {4,5});
        i++;
        
        assertEquals(actual.get(i).interval, new BaseInterval(130,140));
        assertArrayEquals(actual.get(i).values.toArray(), new Integer[] {4});
        i++;
    }
}
