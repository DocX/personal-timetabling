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
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testPutNewConflicts() {
        System.out.println("put");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        int newConflicts;
        newConflicts = instance.put(1, new BaseInterval(10, 50));
        assertEquals(0, newConflicts);

        newConflicts = instance.put(2, new BaseInterval(20, 55));
        assertEquals(2, newConflicts);
        
        newConflicts = instance.put(3, new BaseInterval(10, 30));
        assertEquals(1, newConflicts);
    }
    
    /**
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testPutNewConflicts2() {
        System.out.println("put");
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        int newConflicts;
        newConflicts = instance.put(1, new BaseInterval(10, 50));
        assertEquals(0, newConflicts);

        newConflicts = instance.put(2, new BaseInterval(70, 100));
        assertEquals(0, newConflicts);
        
        newConflicts = instance.put(3, new BaseInterval(45, 80));
        assertEquals(3, newConflicts);
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
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveReturn() {
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(70, 100));
        instance.put(3, new BaseInterval(45, 80));
       
        int newNoConflicts;
        newNoConflicts = instance.remove(3);
        assertEquals(3, newNoConflicts);
    }
    
    
    /**
     * 
     */
    @Test
    public void testValuesInInterval() {
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(70, 100));
        instance.put(3, new BaseInterval(45, 80));
       
        BaseInterval<Integer> interval = new BaseInterval<>(40,90);
        
        // 10  45  50  70  80  100
        // |---|---|---|---|---|
        // 1   13  3   32  2   -
        //    |--------------|
        //    1 13 3   32  2 
        
        List<IntervalMultimap<Integer,Integer>.ValuesInterval> values = instance.valuesInInterval(interval);
        assertEquals(5, values.size());
        
        assertEquals(new BaseInterval<Integer>(40,45), values.get(0).interval);
        assertArrayEquals(new Integer[]{1}, values.get(0).values.toArray());

        assertEquals(new BaseInterval<Integer>(45,50), values.get(1).interval);
        assertArrayEquals(new Integer[]{1,3}, values.get(1).values.toArray());

        assertEquals(new BaseInterval<Integer>(50,70), values.get(2).interval);
        assertArrayEquals(new Integer[]{3}, values.get(2).values.toArray());
        
        assertEquals(new BaseInterval<Integer>(70,80), values.get(3).interval);
        assertArrayEquals(new Integer[]{2,3}, values.get(3).values.toArray());

        assertEquals(new BaseInterval<Integer>(80,90), values.get(4).interval);
        assertArrayEquals(new Integer[]{2}, values.get(4).values.toArray());
    }
    
    /**
     * 
     */
    @Test
    public void testValuesInInterval2() {
        IntervalMultimap<Integer,Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(70, 100));
        instance.put(3, new BaseInterval(45, 80));
       
        BaseInterval<Integer> interval = new BaseInterval<>(40,130);
        
        // 10  45  50  70  80  100
        // |---|---|---|---|---|
        // 1   13  3   32  2   -
        //    |-------------------|
        //    1 13 3   32  2   -
        
        List<IntervalMultimap<Integer,Integer>.ValuesInterval> values = instance.valuesInInterval(interval);
        assertEquals(6, values.size());
        
        assertEquals(new BaseInterval<Integer>(40,45), values.get(0).interval);
        assertArrayEquals(new Integer[]{1}, values.get(0).values.toArray());

        assertEquals(new BaseInterval<Integer>(45,50), values.get(1).interval);
        assertArrayEquals(new Integer[]{1,3}, values.get(1).values.toArray());

        assertEquals(new BaseInterval<Integer>(50,70), values.get(2).interval);
        assertArrayEquals(new Integer[]{3}, values.get(2).values.toArray());
        
        assertEquals(new BaseInterval<Integer>(70,80), values.get(3).interval);
        assertArrayEquals(new Integer[]{2,3}, values.get(3).values.toArray());

        assertEquals(new BaseInterval<Integer>(80,100), values.get(4).interval);
        assertArrayEquals(new Integer[]{2}, values.get(4).values.toArray());
        
        assertEquals(new BaseInterval<Integer>(100,130), values.get(5).interval);
        assertArrayEquals(new Integer[]{}, values.get(5).values.toArray());
    }
    
    
}
