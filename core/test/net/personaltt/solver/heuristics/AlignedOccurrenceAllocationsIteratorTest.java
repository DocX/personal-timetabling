/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.personaltt.model.Occurrence;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author docx
 */
public class AlignedOccurrenceAllocationsIteratorTest {
    
    public AlignedOccurrenceAllocationsIteratorTest() {
    }

   @Test
   public void testAllallocations() {
       BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
       domain.unionWith(0, 20);
       domain.unionWith(30, 40);
       
       IntervalMultimap<Occurrence> intervals = new IntervalMultimap<>();
       Occurrence o = new Occurrence(domain, 10, 20, 1);
       intervals.put(o, new BaseInterval<>(0,20));
       intervals.put(new Occurrence(domain, 5,5,2), new BaseInterval<>(0,5));
       
        AlignedOccurrenceAllocationsIterator allocations = 
                new AlignedOccurrenceAllocationsIterator(o,intervals);
        
        List<BaseInterval<Integer>> expectedAllocations = new ArrayList<>();
        expectedAllocations.add(new BaseInterval<>(0,10));
        expectedAllocations.add(new BaseInterval<>(0,20));
        expectedAllocations.add(new BaseInterval<>(5,15));
        expectedAllocations.add(new BaseInterval<>(5,20));
        expectedAllocations.add(new BaseInterval<>(10,20));
        expectedAllocations.add(new BaseInterval<>(30,40));

        List<BaseInterval<Integer>> actualAllocations = new ArrayList<>();
        for (; allocations.hasNext();) {
           BaseInterval<Integer> baseInterval = allocations.next();
           actualAllocations.add(baseInterval);
       }
        
        assertArrayEquals(expectedAllocations.toArray(),actualAllocations.toArray());
   }
           
}
