package net.personaltt.solver.core;

import net.personaltt.solver.heuristics.MainSolverState;
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
    public float stuckedThresholdCoef = 1.5f;
    
    /**
     * Instance of random used in solver
     */
    Random random;
    
    boolean pause;
    
    SolverState currentSolution;
    
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
     * Default solver
     */
    public Solver() {
        this(new Random(), "net.personaltt.heuristics.RouletteOccurrenceSelection", "net.personaltt.heuristics.MainAllocationSelection");
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
    public SolverState solve(ProblemDefinition problem) {
        OccurrenceSelection occurrenceSelection;
        AllocationSelection allocationSelection;
        try {
            occurrenceSelection = (OccurrenceSelection)Class.forName(occurrenceSelectionClassName).getConstructor().newInstance();
            allocationSelection = (AllocationSelection)Class.forName(allocationSelectionClassName).getConstructor().newInstance();
        } catch(Exception e) {
            System.out.print("Cannot instantiate selections objects");
            return null;
        }
        
        // init solver state
        currentSolution = new MainSolverState();
        currentSolution.init(problem.initialSchedule);
        
        // Start timer
        long startTime = System.currentTimeMillis();

        long stuckedThreshold = Math.max((int)(problem.problemOccurrences.size() * stuckedThresholdCoef), 1000);
        
        // while we have time and is not termination condition met, improve solution 
        while(currentSolution.iterate() && System.currentTimeMillis() - startTime < timeoutLimit) {
            System.out.printf("\nIteration %s\n Cost\t%s\t%s\n", currentSolution.getItearation(), currentSolution.constraintsCost(), currentSolution.optimalCost());
            
            // store best solution
            if (currentSolution.isBetterThanBest()) {
                currentSolution.saveBestSolution();
                System.out.printf(">New best solution found: %s:%s\n", currentSolution.constraintsCost(), currentSolution.optimalCost());
            }
            
            if (pause) {
                System.out.println("Paused");
                synchronized(this) {
                    try {
                        this.wait();
                    } catch(Exception e) {
                        
                    }
                }
                startTime = System.currentTimeMillis();
            }
            
            if ((currentSolution.getItearation() - currentSolution.getLastBestIteration()) > stuckedThreshold) {
                System.out.printf(" Detected stuck. Ending solver.\n");
                break;
            }
            
            // select variable - occurrence
            Occurrence toSolve = occurrenceSelection.select(currentSolution);
            System.out.printf(" Selected occurrence %s at %s:%s\n", toSolve, toSolve.getAllocation(), toSolve.getAllocationCost());
            
            // select and save allocation of occurrence
            OccurrenceAllocation selectedAllocation = allocationSelection.select(currentSolution, toSolve);
            System.out.printf(" Selected allocation: %s:%s \n", selectedAllocation, toSolve.getAllocationCost(selectedAllocation));
            
            boolean conflicts = currentSolution.setAllocation(toSolve, selectedAllocation);
            System.out.printf(" Conflicting %s\n", conflicts);
            
           
        }
        
        // return best solution ever found
        return currentSolution;
    }
    
    public void pause() {
        this.pause = !this.pause;
    }
    
    public void step() {
        synchronized(this) {
            this.notifyAll();
        }
    }

    public void printState() {
        System.out.printf("State:\n");
        for (Map.Entry<Integer, IntervalMultimap.MultimapEdge<Occurrence>> entry : currentSolution.allocationsMultimap().stopsInMap()) {
            System.out.printf("%s: %s\n", entry.getKey(), entry.getValue().getValues());
        }
    }


   
   
    

}
