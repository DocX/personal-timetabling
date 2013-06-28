/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import net.personaltt.solver.heuristics.SimpleSolverState;
import java.util.ArrayList;
import net.personaltt.model.Occurrence;
import net.personaltt.model.ProblemDefinition;
import net.personaltt.model.Schedule;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author docx
 */
public class SimpleSolverStateTest {
    
    public SimpleSolverStateTest() {
    }


    /**
     * Test of cost method, of class SimpleSolverState.
     */
    @Test
    public void testCost() {     
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(10,50);
        
        ProblemDefinition problem = new ProblemDefinition();
        Occurrence o1 = new Occurrence(domain, 10, 10, 1);
        Occurrence o2 = new Occurrence(domain, 10, 10, 2);
        Occurrence o3 = new Occurrence(domain, 10, 10, 3);
        Occurrence o4 = new Occurrence(domain, 10, 10, 4);
        
        problem.addOccurrence(o1, new BaseInterval<>(10,20));
        problem.addOccurrence(o2, new BaseInterval<>(20,30));
        problem.addOccurrence(o3, new BaseInterval<>(30,40));
        problem.addOccurrence(o4, new BaseInterval<>(40,50));
        
        SimpleSolverState solution = new SimpleSolverState();
        solution.init(problem.initialSchedule);
        
        assertEquals(0,solution.constraintsCost());
        
        // remove some
        solution.removeAllocationOf(o1);
        
        assertEquals(0,solution.constraintsCost());
        
        
    }
    
    /**
     * Test of cost method, of class SimpleSolverState.
     */
    @Test
    public void testCost2() {     
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(10,50);
        
        ProblemDefinition problem = new ProblemDefinition();
        Occurrence o1 = new Occurrence(domain, 10, 10, 1);
        Occurrence o2 = new Occurrence(domain, 10, 10, 2);
        Occurrence o3 = new Occurrence(domain, 10, 10, 3);
        Occurrence o4 = new Occurrence(domain, 10, 10, 4);
        
        problem.addOccurrence(o1, new BaseInterval<>(10,20));
        problem.addOccurrence(o2, new BaseInterval<>(15,25));
        problem.addOccurrence(o3, new BaseInterval<>(20,30));
        problem.addOccurrence(o4, new BaseInterval<>(25,30));
        
        SimpleSolverState solution = new SimpleSolverState();
        solution.init(problem.initialSchedule);
        
        assertEquals(4,solution.constraintsCost());
        
        // remove some
        solution.removeAllocationOf(o2);
        
        assertEquals(2,solution.constraintsCost());
        
        // remove some
        solution.removeAllocationOf(o3);
        
        assertEquals(0,solution.constraintsCost());
        
    }
}
