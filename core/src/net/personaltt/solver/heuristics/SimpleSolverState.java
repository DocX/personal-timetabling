/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.model.Schedule;
import net.personaltt.solver.core.SolverSolution;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

/**
 * Simple solver solution. It uses intervals multimap as backing store of
 * allocations. Also keeps updated Schedule all time
 * @author docx
 */
public class SimpleSolverState implements SolverState {

    IntervalMultimap<Occurrence> allocationMultimap;
    
    Schedule solutionSchedule;
    
    long assignedCost;
    
    SolverSolution bestSolution;
    
    int bestIteration;
    
    int currentIteration;
    
    List<Occurrence> unassignedOccurrences;
    
    @Override
    public void init(Schedule schedule) {
        allocationMultimap = new IntervalMultimap<>();
        solutionSchedule = new Schedule();
        unassignedOccurrences = new ArrayList<>(schedule.numberOccurrences());
        assignedCost = 0;

        currentIteration = 0;
        bestSolution = null;
        bestIteration = 0;
        
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : schedule.getOccurrencesAllocations()) {
            unassignedOccurrences.add((Occurrence)entry.getKey().clone());
        }
    }

    @Override
    public long optimalCost() {
        return assignedCost;
    }

    @Override
    public long constraintsCost() {
       return unassignedOccurrences.size();
    }

    @Override
    public IntervalMultimap<Occurrence> allocationsMultimap() {
        return allocationMultimap;
    }

    @Override
    public OccurrenceAllocation unassigneAllocation(Occurrence toSolve) {
        allocationMultimap.remove(toSolve);
        
        // subtract removed allocation cost
        assignedCost -= toSolve.getAllocationCost();
        toSolve.setAllocation(null);
        
        unassignedOccurrences.add(toSolve);
   
        return solutionSchedule.removeAllocation(toSolve);
    }

    @Override
    public List<Occurrence> assigneAllocation(Occurrence toSolve, OccurrenceAllocation solvingAllocation) {       
        unassignedOccurrences.remove(toSolve);
        
        solutionSchedule.setAllocation(toSolve, solvingAllocation);
        toSolve.setAllocation(solvingAllocation);
        
        List<Occurrence> newConflicts = allocationMultimap.put(toSolve, solvingAllocation.toInterval());
        assignedCost += toSolve.getAllocationCost();

        return newConflicts;
    }
    
    @Override
    public boolean setAllocation(Occurrence toSolve, OccurrenceAllocation allocation) {
        // update occurrence with allocation and unassigne conflicting occurrences
        
        if (toSolve.getAllocation() != null) {
            // remove from multimap
            allocationMultimap.remove(toSolve);
        }
        
        List<Occurrence> newConflicting = assigneAllocation(toSolve, allocation);
        if (newConflicting != null) {
            for (Occurrence occurrence : newConflicting) {
                unassigneAllocation(occurrence);
            }
        }
        
        return newConflicting != null && newConflicting.size() > 0;
    }

    @Override
    public Schedule getSchedule() {
        return solutionSchedule;
    }

    public SolverSolution cloneSolution() {
        final long optimalCost = this.optimalCost();
        final long constraintCost = this.constraintsCost();
        final Schedule clonedSchedule = (Schedule)this.getSchedule().allocationsClone();
        
        return new SolverSolution() {
            
            @Override
            public long optimalCost() {
                return optimalCost;
            }

            @Override
            public long constraintsCost() {
                return constraintCost;
            }
            
            

            @Override
            public Schedule getSchedule() {
                return clonedSchedule;
            }
        };
    }

    /**
     * Coparison of solution. Better is less constraint cost, then optimal cost. 
     * If current is better, positive number is returned, if current is worse than given,
     * negative number is returned. If both are equal, zero is returned.
     * @param o
     * @return 
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof SolverSolution) {
            int constraintCostCmp = Long.compare(((SolverSolution)o).constraintsCost(), constraintsCost());
            
            return constraintCostCmp != 0 ? constraintCostCmp : Long.compare(((SolverSolution)o).optimalCost() , optimalCost());
        }
        
        return 1;
    }


    @Override
    public boolean iterate() {
        if (optimalCost() == 0 && constraintsCost() == 0) {
            return false;
        }
        currentIteration++;
        return true;
    }

    @Override
    public boolean isBetterThanBest() {
        return bestSolution == null || this.compareTo(bestSolution) > 0;
    }

    @Override
    public void saveBestSolution() {
        bestSolution = cloneSolution();
        bestIteration = currentIteration;
    }

    @Override
    public int getLastBestIteration() {
        return bestSolution == null ? currentIteration : bestIteration;
    }

    @Override
    public int getItearation() {
        return currentIteration;
    }

    @Override
    public SolverSolution getBestSolution() {
        return bestSolution;
    }

    @Override
    public List<Occurrence> getUnassignedOccurrences() {
        return unassignedOccurrences;
    }
   
    
}
