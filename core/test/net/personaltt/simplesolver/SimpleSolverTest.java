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
        System.out.println();
        System.out.println("Test start: no conflict");
        // This is rather functional test than unit test
        
        // simple problem
        // all occurrences will have same domain
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(10,50);
        
        // problem occurrences with no conflict
        ProblemDefinition problem = new ProblemDefinition();
        problem.addOccurrence(new Occurrence(domain, 5, 10, 1), new BaseInterval<>(20, 30));
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
        System.out.println();
        System.out.println("Test start: simple conflict");
        
        // This is rather functional test than unit test
        
        // simple problem
        // all occurrences will have same domain
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(10,50);
        
        // problem occurrences with conflict of 1 and 2
        ProblemDefinition problem = new ProblemDefinition();
        problem.addOccurrence(new Occurrence(domain, 5, 10, 1), new BaseInterval<>(10, 20));
        problem.addOccurrence(new Occurrence(domain, 5, 10, 2), new BaseInterval<>(15, 25));
        problem.addOccurrence(new Occurrence(domain, 5, 10, 3), new BaseInterval<>(30, 35));
        
        
        // solve
        SimpleSolver solver = new SimpleSolver(new Random(20130601));
        Schedule solved = solver.solve(problem);
        
        // solved schedule should be equal to problem initial but not the same object
        assertNotSame(problem.initialSchedule, solved);        
        
        // should not have conflict
        assertFalse(solved.hasConflict());
    }
    
    /**
     * Test of solve method, of class SimpleSolver. 
     * With conflicting problem and contrained domain
     * with solution
     */
    @Test
    public void testSolveConflictingProblemConstrained() {
        System.out.println();
        System.out.println("Test start: constrained conflict");
        
        // This is rather functional test than unit test
        
        // simple problem
        // all occurrences will have same domain
        // length of domain is sum of minimal lengths of all occurrences
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(10,25);
        
        // problem occurrences with all conflicting
        ProblemDefinition problem = new ProblemDefinition();
        problem.addOccurrence(new Occurrence(domain, 5, 10, 1), new BaseInterval<>(10, 20));
        problem.addOccurrence(new Occurrence(domain, 5, 10, 2), new BaseInterval<>(12, 22));
        problem.addOccurrence(new Occurrence(domain, 5, 10, 3), new BaseInterval<>(15, 25));
        
        // solve
        SimpleSolver solver = new SimpleSolver(new Random(20130601));
        Schedule solved = solver.solve(problem);
        
        // solved schedule should be equal to problem initial but not the same object
        assertNotSame(problem.initialSchedule, solved);        
        
        // should not have conflict
        assertFalse(solved.hasConflict());
    }
    
    /**
     * Test of solve method, of class SimpleSolver. 
     * With conflicting problem and contrained domain
     * with solution. Initial state has no free domain for any of 
     * occurrence
     */
    @Test
    public void testSolveConflictingProblemConstrained2() {
        System.out.println();
        System.out.println("Test start: constrained conflict 2");
        
        // This is rather functional test than unit test
        
        // simple problem
        // all occurrences will have same domain
        // length of domain is sum of minimal lengths of all occurrences
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(10,25);
        
        // problem occurrences with all conflicting
        ProblemDefinition problem = new ProblemDefinition();
        problem.addOccurrence(new Occurrence(domain, 5, 15, 1), new BaseInterval<>(10, 25));
        problem.addOccurrence(new Occurrence(domain, 5, 15, 2), new BaseInterval<>(10, 25));
        problem.addOccurrence(new Occurrence(domain, 5, 15, 3), new BaseInterval<>(10, 25));
        
        // solution is duration of all 5 and each on one of positions 10,15 and 20
        
        // solve
        SimpleSolver solver = new SimpleSolver(new Random(20130601));
        Schedule solved = solver.solve(problem);
        
        // solved schedule should be equal to problem initial but not the same object
        assertNotSame(problem.initialSchedule, solved);        
        
        // should not have conflict
        assertFalse(solved.hasConflict());
    }
    
    /**
     * Test of solve method, of class SimpleSolver. 
     * With conflicting problem and contrained domain
     * with solution. Initial state has no free domain for any of 
     * occurrence
     */
    @Test
    public void testSolveConflictingProblemConstrained3() {
        System.out.println();
        System.out.println("Test start: constrained conflict 3");
        
        // This is rather functional test than unit test
        
        // simple problem
        // all occurrences will have same domain
        // length of domain is sum of minimal lengths of all occurrences
        BaseIntervalsSet<Integer> domain1 = new BaseIntervalsSet<>();
        domain1.unionWith(10,30);
        
        BaseIntervalsSet<Integer> domain2 = new BaseIntervalsSet<>();
        domain2.unionWith(20,40);
        
        // problem occurrences with all conflicting
        ProblemDefinition problem = new ProblemDefinition();
        problem.addOccurrence(new Occurrence(domain1, 10, 10, 1), new BaseInterval<>(10, 20));
        problem.addOccurrence(new Occurrence(domain1, 10, 10, 2), new BaseInterval<>(15, 25));
        problem.addOccurrence(new Occurrence(domain2, 10, 10, 3), new BaseInterval<>(20, 30));
        
        // solution is duration of all 5 and each on one of positions 10,15 and 20
        
        // solve
        SimpleSolver solver = new SimpleSolver(new Random(20130601));
        Schedule solved = solver.solve(problem);
        
        // solved schedule should be equal to problem initial but not the same object
        assertNotSame(problem.initialSchedule, solved);        
        
        // should not have conflict
        assertFalse(solved.hasConflict());
    }
}
