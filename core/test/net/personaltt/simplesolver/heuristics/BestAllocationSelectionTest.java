/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import net.personaltt.solver.heuristics.BestAllocationSelection;
import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author docx
 */
public class BestAllocationSelectionTest {
    
    public BestAllocationSelectionTest() {
    }

    
    @Test
    public void testOneStepBeforeEnd() {
        
        IntervalMultimap<Integer, Occurrence> schedule = new IntervalMultimap<>();
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(0, 30);
        
        createOccurrenceAllocation(schedule, domain, 10, 30, 0, 10, 1);
        createOccurrenceAllocation(schedule, domain, 10, 30, 20, 30, 2);
        
        Occurrence toSolve = new Occurrence(domain, 10, 30, 3);
        toSolve.setAllocation(new OccurrenceAllocation(10, 10));
        
        BestAllocationSelection selection = new BestAllocationSelection();
        OccurrenceAllocation allocation = selection.select(schedule, toSolve );
        
        assertEquals(10, allocation.getDuration());
        assertEquals(10, allocation.getStart());
    }
    
    private void createOccurrenceAllocation(IntervalMultimap<Integer, Occurrence> schedule, BaseIntervalsSet<Integer> domain, int min, int max, int start, int end, int id) {
        Occurrence o = new Occurrence(domain, min, max, id);
        o.setAllocation(new OccurrenceAllocation(start, end - start));
        
        schedule.put(o, new BaseInterval<>(start, end));
    }

}
