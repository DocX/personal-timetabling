/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils.intervalmultimap;

import net.personaltt.utils.intervalmultimap.IntervalMultimap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.personaltt.model.Occurrence;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.Iterables;
import net.personaltt.utils.ValuedInterval;
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
        IntervalMultimap<Object> instance = new IntervalMultimap<>();
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
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
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
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        long newConflicts;
        newConflicts = instance.put(1, new BaseInterval(10, 50));
        assertEquals(0, newConflicts);

        newConflicts = instance.put(2, new BaseInterval(20, 55));
        assertEquals(60, newConflicts);
        
        newConflicts = instance.put(3, new BaseInterval(10, 30));
        assertEquals(60, newConflicts);
    }
    
    /**
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testPutNewConflicts2() {
        System.out.println("put");
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        long newConflicts;
        newConflicts = instance.put(1, new BaseInterval(10, 50));
        assertEquals(0, newConflicts);

        newConflicts = instance.put(2, new BaseInterval(70, 100));
        assertEquals(0, newConflicts);
        
        newConflicts = instance.put(3, new BaseInterval(45, 80));
        assertEquals(30, newConflicts);
    }

    /**
     * Test of remove method, of class IntervalMultimap.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(20, 55));
        instance.put(3, new BaseInterval(10, 30));
        
        instance.remove(2);
        
        // |----|-----|-----|--|
        // 10   20    30    50 55
        // 1,3  /     1     -  /
        
        assertEquals(3, instance.edges.size());
        assertNotNull(instance.edges.get(10));
        assertNotNull(instance.edges.get(30));
        assertNotNull(instance.edges.get(50));
        assertNull(instance.edges.get(55));
        
        assertArrayEquals(new Integer[] {1,3}, instance.edges.get(10).toArray());
        assertArrayEquals(new Integer[] {1}, instance.edges.get(30).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(50).toArray());
    }

    /**
     * Test of remove method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveFirst() {
        System.out.println("remove");
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
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
     * Test of remove method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveLast() {
        System.out.println("remove");
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(30, 50));
        instance.put(3, new BaseInterval(30, 50));

        instance.remove(1);
        
        assertEquals(2, instance.edges.size());
        assertNotNull(instance.edges.get(30));
        assertNotNull(instance.edges.get(50));
        assertNull(instance.edges.get(10));
        
        assertArrayEquals(new Integer[] {2,3}, instance.edges.get(30).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(50).toArray());
        
    }
    
    /**
     * Test of remove method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveLast2() {
        System.out.println("remove");
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(4, new BaseInterval(40, 60));
        instance.put(2, new BaseInterval(30, 50));
        instance.put(3, new BaseInterval(30, 50));

        instance.remove(4);
        
        assertEquals(3, instance.edges.size());
        assertNotNull(instance.edges.get(30));
        assertNotNull(instance.edges.get(50));
        assertNotNull(instance.edges.get(10));
        
        assertArrayEquals(new Integer[] {1}, instance.edges.get(10).toArray());
        assertArrayEquals(new Integer[] {1,2,3}, instance.edges.get(30).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(50).toArray());
        
    }
    
    /**
     * Test of remove method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveLast3() {
        System.out.println("remove");
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        for (int i = 0; i < 1000; i++) {
            instance.put(i, new BaseInterval(0, 10));
        }
    
        instance.remove(10);
        instance.put(10,new BaseInterval(9990, 10000));
        instance.remove(100);
        instance.put(100,new BaseInterval(9990, 10000));
        
        assertEquals(4, instance.edges.size());
        assertNotNull(instance.edges.get(0));
        assertNotNull(instance.edges.get(10));
        assertNotNull(instance.edges.get(9990));
        assertNotNull(instance.edges.get(10000));
        
        assertArrayEquals(new Integer[] {10,100}, instance.edges.get(9990).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(10000).toArray());
        
    }
    
    
    /**
     * Test of remove method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveLast4() {
        System.out.println("remove");
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        // need stops
        // 0    10   20   30
        // |    |    |    | 
        // 1,2  2    3    -   
        
        instance.put(1, new BaseInterval(0, 10));
        instance.put(2, new BaseInterval(0, 20));
        instance.put(3, new BaseInterval(20, 30));
    
        // remove 2
        instance.remove(2);
        
        // should be
        // 0  10  20 30
        // |  |   |  |
        // 1  -   3  -
        
        assertEquals(4, instance.edges.size());
        assertNotNull(instance.edges.get(0));
        assertNotNull(instance.edges.get(10));
        assertNotNull(instance.edges.get(20));
        assertNotNull(instance.edges.get(30));
        
        assertArrayEquals(new Integer[] {1}, instance.edges.get(0).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(10).toArray());
        assertArrayEquals(new Integer[] {3}, instance.edges.get(20).toArray());
        assertArrayEquals(new Integer[] {}, instance.edges.get(30).toArray());
        
    }
    
    /**
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveReturn() {
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(70, 100));
        instance.put(3, new BaseInterval(45, 80));
       
        long newNoConflicts;
        newNoConflicts = instance.remove(3);
        assertEquals(30, newNoConflicts);
    }
    
     /**
     * Test of put method, of class IntervalMultimap.
     */
    @Test
    public void testRemoveReturn2() {
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 20));
        instance.put(2, new BaseInterval(10, 20));
        instance.put(3, new BaseInterval(10, 20));
       
        long newNoConflicts;
        newNoConflicts = instance.remove(3);
        assertEquals(40, newNoConflicts);
        
        newNoConflicts = instance.remove(1);
        assertEquals(20, newNoConflicts);
    }
    
    
    /**
     * 
     */
    @Test
    public void testValuesInInterval() {
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(70, 100));
        instance.put(3, new BaseInterval(45, 80));
       
        BaseInterval<Integer> interval = new BaseInterval<>(40,90);
        
        // 10  45  50  70  80  100
        // |---|---|---|---|---|
        // 1   13  3   32  2   -
        //    |--------------|
        //    1 13 3   32  2 
        
        Iterable<ValuedInterval<Integer, List<Integer>>> valuesIterable = instance.valuesInInterval(interval);
        ArrayList<ValuedInterval<Integer, List<Integer>>> values = new ArrayList<>();
        for (ValuedInterval<Integer, List<Integer>> valuedInterval : valuesIterable) {
            values.add(valuedInterval);
        }
        assertEquals(5, values.size());
        
        assertEquals(new BaseInterval<Integer>(40,45), values.get(0));
        assertArrayEquals(new Integer[]{1}, values.get(0).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(45,50), values.get(1));
        assertArrayEquals(new Integer[]{1,3}, values.get(1).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(50,70), values.get(2));
        assertArrayEquals(new Integer[]{3}, values.get(2).getValues().toArray());
        
        assertEquals(new BaseInterval<Integer>(70,80), values.get(3));
        assertArrayEquals(new Integer[]{2,3}, values.get(3).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(80,90), values.get(4));
        assertArrayEquals(new Integer[]{2}, values.get(4).getValues().toArray());
    }
    
    /**
     * 
     */
    @Test
    public void testValuesInInterval2() {
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(70, 100));
        instance.put(3, new BaseInterval(45, 80));
       
        BaseInterval<Integer> interval = new BaseInterval<>(40,130);
        
        // 10  45  50  70  80  100
        // |---|---|---|---|---|
        // 1   13  3   32  2   -
        //    |-------------------|
        //    1 13 3   32  2   -
        
        Iterable<ValuedInterval<Integer, List<Integer>>> valuesIterable = instance.valuesInInterval(interval);
        ArrayList<ValuedInterval<Integer, List<Integer>>> values = new ArrayList<>();
        for (ValuedInterval<Integer, List<Integer>> valuedInterval : valuesIterable) {
            values.add(valuedInterval);
        }

        assertEquals(6, values.size());
        
        assertEquals(new BaseInterval<Integer>(40,45), values.get(0));
        assertArrayEquals(new Integer[]{1}, values.get(0).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(45,50), values.get(1));
        assertArrayEquals(new Integer[]{1,3}, values.get(1).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(50,70), values.get(2));
        assertArrayEquals(new Integer[]{3}, values.get(2).getValues().toArray());
        
        assertEquals(new BaseInterval<Integer>(70,80), values.get(3));
        assertArrayEquals(new Integer[]{2,3}, values.get(3).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(80,100), values.get(4));
        assertArrayEquals(new Integer[]{2}, values.get(4).getValues().toArray());
        
        assertEquals(new BaseInterval<Integer>(100,130), values.get(5));
        assertArrayEquals(new Integer[]{}, values.get(5).getValues().toArray());
    }
    
    /**
     * 
     */
    @Test
    public void testValuesInInterval3() {
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 50));
        instance.put(2, new BaseInterval(70, 100));
        instance.put(3, new BaseInterval(45, 50));
       
        BaseInterval<Integer> interval = new BaseInterval<>(40,130);
        
        // 10  45  50  70    100
        // |---|---|---|-----|
        // 1   13  -   2     -
        //    |-------------------|
        //    1 13 -   2     -
        
        Iterable<ValuedInterval<Integer, List<Integer>>> valuesIterable = instance.valuesInInterval(interval);
        ArrayList<ValuedInterval<Integer, List<Integer>>> values = new ArrayList<>();
        for (ValuedInterval<Integer, List<Integer>> valuedInterval : valuesIterable) {
            values.add(valuedInterval);
        }
        assertEquals(5, values.size());
        
        assertEquals(new BaseInterval<Integer>(40,45), values.get(0));
        assertArrayEquals(new Integer[]{1}, values.get(0).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(45,50), values.get(1));
        assertArrayEquals(new Integer[]{1,3}, values.get(1).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(50,70), values.get(2));
        assertArrayEquals(new Integer[]{}, values.get(2).getValues().toArray());
        
        assertEquals(new BaseInterval<Integer>(70,100), values.get(3));
        assertArrayEquals(new Integer[]{2}, values.get(3).getValues().toArray());

        
        assertEquals(new BaseInterval<Integer>(100,130), values.get(4));
        assertArrayEquals(new Integer[]{}, values.get(4).getValues().toArray());
    }
    
    /**
     * 
     */
    @Test
    public void testValuesIntervals1() {
        IntervalMultimap<Integer> instance = new IntervalMultimap<>();
        instance.put(1, new BaseInterval(10, 20));
        instance.put(2, new BaseInterval(20, 30));
        instance.put(3, new BaseInterval(30, 40));
        instance.put(4, new BaseInterval(30, 40));
        
        ArrayList<ValuedInterval<Integer, List<Integer>>> values = Iterables.toArrayList(instance.valuesIntervals());
        
        assertEquals(3, values.size());
        
        assertEquals(new BaseInterval<Integer>(10,20), values.get(0));
        assertArrayEquals(new Integer[]{1}, values.get(0).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(20,30), values.get(1));
        assertArrayEquals(new Integer[]{2}, values.get(1).getValues().toArray());

        assertEquals(new BaseInterval<Integer>(30,40), values.get(2));
        assertArrayEquals(new Integer[]{3,4}, values.get(2).getValues().toArray());
        
        
    }
    
    
}
