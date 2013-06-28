package net.personaltt.solver.core;

import net.personaltt.solver.heuristics.SimpleSolverState;
import java.util.Map;
import java.util.Random;
import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.model.ProblemDefinition;
import net.personaltt.model.Schedule;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

/**
 * Simple solver finds any feasible schedule for personal timetabling problem.
 * @author docx
 */
public class Solver {
   
    /**
     * Timeout in ms after which solver stops
     */
    public long timeoutLimit = 15000;
    
    /*
     * Coeficient of stucket threshold, multiplied with number of occurrences in
     * problem.
     */
    public int stuckedThresholdCoef = 3;
    
    /**
     * Instance of random used in solver
     */
    Random random;
    
    /**
     * Class name of occurrence selection used in neighbour selection
     */
    String occurrenceSelectionClassName;
    
    /**
     * Class name of allocation of occurrence selection used in neghbour selection
     */
    String allocationSelectionClassName;
    
    public Solver(String occurrenceSelectionClassName, String allocationSelectionClassName) {
        this(new Random(), occurrenceSelectionClassName,allocationSelectionClassName);
    }
    
    public Solver(Random random, String occurrenceSelectionClassName, String allocationSelectionClassName) {
        this.random = random;
        this.allocationSelectionClassName = allocationSelectionClassName;
        this.occurrenceSelectionClassName = occurrenceSelectionClassName;
    }
    
    /**
     * Solves given problem.
     * It uses standart neigbourh selection in two steps: selecting variable (occurrence)
     * and then value for it (allocation).
     * It detects stuck on local extreme. If selection heuristics are good and 
     * avoids stucking in local, suboptimal, extremes, this will detect global 
     * extreme.
     * 
     * @param problem
     * @return solution
     */
    public Schedule solve(ProblemDefinition problem) {
        OccurrenceSelection occurrenceSelection;
        AllocationSelection allocationSelection;
        try {
            occurrenceSelection = (OccurrenceSelection)Class.forName(occurrenceSelectionClassName).getConstructor().newInstance();
            allocationSelection = (AllocationSelection)Class.forName(allocationSelectionClassName).getConstructor().newInstance();
        } catch(Exception e) {
            System.out.print("Cannot instantiate selections objects");
            return null;
        }
        
        // init solution
        SolverSolution bestSolution = null;
        SolverState currentSolution = new SimpleSolverState();
        currentSolution.init(problem.initialSchedule);
        
        // Start timer
        long startTime = System.currentTimeMillis();
        long iteration = 0;

        // Stuck detection variables
        long firstCostIteration = 0;
        SolverSolution prevSolutionCost = null;
        
        long stuckedThreshold = Math.max(problem.problemOccurrences.size() * stuckedThresholdCoef, 1000);
        
        // while we have time and is not termination condition met, improve solution 
        while(!currentSolution.terminationCondition() && System.currentTimeMillis() - startTime < timeoutLimit) {
            System.out.printf("Iteration %s, Cost %s:%s\n", iteration, currentSolution.constraintsCost(), currentSolution.optimalCost());
            
            if (currentSolution.compareTo(prevSolutionCost) == 0) {
                if ((iteration - firstCostIteration) > stuckedThreshold) {
                    System.out.printf(" Detected stuck. Ending solver.\n");
                    break;
                }
            } else {
                prevSolutionCost = currentSolution.cloneCost();
                firstCostIteration = iteration;
            }
            
            // select variable - occurrence
            Occurrence toSolve = occurrenceSelection.select(currentSolution);
            System.out.printf(" Selected occurrence %s at %s\n", toSolve, toSolve.getAllocation());
            
            // select and save allocation of occurrence
            OccurrenceAllocation selectedAllocation = allocationSelection.select(currentSolution, toSolve);
            System.out.printf(" Selected allocation: %s\n", selectedAllocation);
            
            boolean conflicts = currentSolution.updateAllocation(toSolve, selectedAllocation);
            System.out.printf(" Conflicting %s\n", conflicts);
            
            // store best solution
            if (currentSolution.compareTo(bestSolution) > 0) {
                bestSolution = currentSolution.cloneSolution();
                System.out.printf(">New best solution found: %s:%s\n", currentSolution.constraintsCost(), currentSolution.optimalCost());
            }
            
            iteration++;
        }
        
        // return best solution ever found
        return bestSolution.getSchedule();
    }

    private void printState(SolverState currentSolution) {
        System.out.printf("State:\n");
        for (Map.Entry<Integer, IntervalMultimap.MultimapEdge<Occurrence>> entry : currentSolution.allocationsMultimap().stopsInMap()) {
            System.out.printf("%s: %s\n", entry.getKey(), entry.getValue().getValues());
        }
    }


   
   
    

}
