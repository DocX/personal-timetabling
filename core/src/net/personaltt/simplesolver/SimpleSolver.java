package net.personaltt.simplesolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.ProblemDefinition;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;
import net.personaltt.utils.IntervalMultimap;
import net.personaltt.utils.IntervalMultimap.MultiIntervalStop;

/**
 * Simple solver finds any feasible schedule for personal timetabling problem.
 * @author docx
 */
public class SimpleSolver {
   
    /**
     * Timeout in ms after which solver stops
     */
    public long timeoutLimit = 15000;
    
    /**
     * Instance of random used in solver
     */
    Random random;
    
    public SimpleSolver() {
        random = new Random();
    }
    
    public SimpleSolver(Random random) {
        this.random = random;
    }
    
    /**
     * Solves given problem. It is trying to find feasible solution (solution with
     * minimal conflicting area). Does no optimalization.
     * 
     * 
     * @param problem
     * @return solution
     */
    public Schedule solve(ProblemDefinition problem) {
        // init subroutines

        // Occurrence selection
        OccurrenceSelection selection = new RouletteSelection();

        AllocationSelection allocationSelection = new SimpleAllocationSelection();
        
        // Initialize solver working variables
        
        // clones initial schedule from problem definition
        SolverSolution currentSolution = new SimpleSolverSolution();
        currentSolution.init((Schedule)problem.initialSchedule.clone());
        
        // Start timer
        long startTime = System.currentTimeMillis();
        long iteration = 0;
        
        // while there is conflict, solve it
        while(!currentSolution.terminationCondition() && System.currentTimeMillis() - startTime < timeoutLimit) {
            System.out.printf("Iteration %s, Cost %s\n", iteration, currentSolution.cost());
            
            // Get random conflicting occurrence and its current allocation
            // with more probability on first items in arrays, which have more conflicts
            Occurrence toSolve = selection.select(currentSolution.allocationsMultimap());

            // remove solving occurrence allocation for allocation selection
            OccurrenceAllocation toSolveAllocation = currentSolution.removeAllocationOf(toSolve);
            
            System.out.printf("Selected occurrence %s at %s\n", toSolve, toSolveAllocation);
            
            // select allocation of solving occurrence
            OccurrenceAllocation solvingAllocation = allocationSelection.select(
                    currentSolution.allocationsMultimap(), toSolve);
           
            // set selected allocation
            boolean conflicts = currentSolution.setAllocation(toSolve, solvingAllocation);
            
            System.out.printf("Resolved as: %s With conflict: %s\n", solvingAllocation, conflicts);
            
            iteration++;
        }
        
        return currentSolution.getSchedule();
        
    }


   
   
    

}
