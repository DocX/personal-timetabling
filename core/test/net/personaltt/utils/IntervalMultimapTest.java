/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

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

    /**
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testPut1() {
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
}
