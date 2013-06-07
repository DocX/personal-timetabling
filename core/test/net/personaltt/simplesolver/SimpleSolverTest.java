/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.ProblemDefinition;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author docx
 */
public class SimpleSolverTest {
    
    
    public SimpleSolverTest() {
    }

    /**
     * Test of solve method, of class SimpleSolver.
     */
    @Test
    public void testSolve() {
        
        // This is rather functional test than unit test
        
        // simple problem
        // all occurrences will have same domain
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(10,50);
        
        // problem occurrences with no conflict
        ProblemDefinition problem = new ProblemDefinition();
        problem.addOccurrence(new Occurrence(domain, 5, 10, 1), new BaseInterval<>(0, 5));
        problem.addOccurrence(new Occurrence(domain, 5, 10, 2), new BaseInterval<>(10, 20));
        problem.addOccurrence(new Occurrence(domain, 5, 10, 3), new BaseInterval<>(30, 35));
        
        
        // solve
        SimpleSolver solver = new SimpleSolver(new Random(20130601));
        Schedule solved = solver.solve(problem);
        
        // solved schedule should be equal to problem initial but not the same object
        assertEquals(problem.initialSchedule, solved);
        assertNotSame(problem.initialSchedule, solved);        
    }
    
    /**
     * Test of solve method, of class SimpleSolver. 
     * With conflicting problem
     */
    @Test
    public void testSolveConflictingProblem() {
        
        // This is rather functional test than unit test
        
        // simple problem
        // all occurrences will have same domain
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(10,50);
        
        // problem occurrences with no conflict
        ProblemDefinition problem = new ProblemDefinition();
        problem.addOccurrence(new Occurrence(domain, 5, 10, 1), new BaseInterval<>(0, 10));
        problem.addOccurrence(new Occurrence(domain, 5, 10, 2), new BaseInterval<>(5, 15));
        problem.addOccurrence(new Occurrence(domain, 5, 10, 3), new BaseInterval<>(30, 35));
        
        
        // solve
        SimpleSolver solver = new SimpleSolver(new Random(20130601));
        Schedule solved = solver.solve(problem);
        
        // solved schedule should be equal to problem initial but not the same object
        assertNotSame(problem.initialSchedule, solved);        
        
        // should not have conflict
        assertFalse(solved.hasConflict());
    }
}
