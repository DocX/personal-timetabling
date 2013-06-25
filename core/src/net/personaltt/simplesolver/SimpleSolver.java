package net.personaltt.simplesolver;

import java.util.List;
import net.personaltt.simplesolver.heuristics.SimpleAllocationSelection;
import net.personaltt.simplesolver.heuristics.RouletteSelection;
import net.personaltt.simplesolver.heuristics.SimpleSolverState;
import java.util.Map;
import java.util.Random;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.ProblemDefinition;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.IntervalMultimap;

/**
 * Simple solver finds any feasible schedule for personal timetabling problem.
 * @author docx
 */
public class SimpleSolver {
   
    /**
     * Timeout in ms after which solver stops
     */
    public long timeoutLimit = 15000;
    
    /*
     * Number of iterations that do not changes cost of solutions 
     * considered as that solver is stuck
     */
    public int stuckedThreshold = 5000;
    
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
        SolverSolution bestSolution = null;
        SolverState currentSolution = new SimpleSolverState();
        currentSolution.init(problem.initialSchedule);
        
        // Start timer
        long startTime = System.currentTimeMillis();
        long iteration = 0;
        long prevCost = -1;
        long firstCostIteration = 0;
        
        // while there is conflict, solve it
        while(!currentSolution.terminationCondition() && System.currentTimeMillis() - startTime < timeoutLimit) {
            System.out.printf("Iteration %s, Cost %s\n", iteration, currentSolution.cost());
            
            if (prevCost == currentSolution.cost()) {
                if ((iteration - firstCostIteration) > stuckedThreshold) {
                    System.out.printf("Detected stuck. Ending solver.\n");
                    break;
                }
            } else {
                prevCost = currentSolution.cost();
                firstCostIteration = iteration;
            }
            
            // Get random conflicting occurrence and its current allocation
            // with more probability on first items in arrays, which have more conflicts
            Occurrence toSolve =  selection.select(currentSolution.allocationsMultimap());

            //printState(currentSolution);
             
            // remove solving occurrence allocation from state,
            // in order to allocation selection can select place on schedule without
            // allocating occurrence itself.
            OccurrenceAllocation toSolveAllocation = currentSolution.removeAllocationOf(toSolve);
            
            //System.out.printf("Selected occurrence %s at %s\n", toSolve, toSolveAllocation);
            
            // select allocation of solving occurrence
            OccurrenceAllocation solvingAllocation = allocationSelection.select(
                    currentSolution.allocationsMultimap(), toSolve);
           
            // set selected allocation
            boolean conflicts = currentSolution.setAllocation(toSolve, solvingAllocation);
            
            //System.out.printf("Resolved as: %s With conflict: %s\n", solvingAllocation, conflicts);
            
            // store best solution
            if (bestSolution == null || bestSolution.cost() > currentSolution.cost()) {
                bestSolution = currentSolution.cloneSolution();
            }
            
            iteration++;

        }
        
        return bestSolution.getSchedule();
        
    }

    private void printState(SolverState currentSolution) {
        System.out.printf("State:\n");
        for (Map.Entry<Integer, IntervalMultimap.MultimapEdge<Occurrence>> entry : currentSolution.allocationsMultimap().stopsInMap()) {
            System.out.printf("%s: %s\n", entry.getKey(), entry.getValue().getValues());
        }
    }


   
   
    

}
