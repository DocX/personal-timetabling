/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

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
    
    long conflictingOccurrences;
    
    long durationsSum;
    
    long maxDurationsSum;
    
    SolverSolution bestSolution;
    int bestIteration;
    
    int currentIteration;
    
    @Override
    public void init(Schedule schedule) {
        allocationMultimap = new IntervalMultimap<>();
        solutionSchedule = (Schedule)schedule.deepClone();
        conflictingOccurrences = 0;
        durationsSum = 0;
        maxDurationsSum = 0;
        currentIteration = 0;
        bestSolution = null;
        bestIteration = 0;
        
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : solutionSchedule.getOccurrencesAllocations()) {
            long newConflicts = allocationMultimap.put(entry.getKey(), entry.getValue().toInterval());
            conflictingOccurrences += newConflicts;
            if (entry.getValue().getDuration() > entry.getKey().getMaxDuration()) {
                throw new IllegalArgumentException("Duration cannot be greater than max duration");
            }
            
            durationsSum += entry.getValue().getDuration();
            maxDurationsSum += entry.getKey().getMaxDuration();
        }
    }

    @Override
    public long optimalCost() {
        //return conflictingOccurrences;
        return (maxDurationsSum - durationsSum);
    }

    @Override
    public long constraintsCost() {
       return conflictingOccurrences;
    }

    @Override
    public IntervalMultimap<Occurrence> allocationsMultimap() {
        return allocationMultimap;
    }

    @Override
    public OccurrenceAllocation removeAllocationOf(Occurrence toSolve) {
        long conflictsRemoved = allocationMultimap.remove(toSolve);
        conflictingOccurrences -= conflictsRemoved;
        OccurrenceAllocation aloc = solutionSchedule.getAllocationOf(toSolve);
        
        // subtract removed allocation duration
        durationsSum -= aloc.getDuration();
        
        return aloc;
    }

    @Override
    public boolean setAllocation(Occurrence toSolve, OccurrenceAllocation solvingAllocation) {
        // debugging if, should not happen
        if (toSolve.getMaxDuration() < solvingAllocation.getDuration()) {
            throw new IllegalStateException("Allocation cannot be longer than max duration of occurrence");
        }
        
        solutionSchedule.getAllocationOf(toSolve).set(solvingAllocation);

        long newConflicts = allocationMultimap.put(toSolve, solvingAllocation.toInterval());
        conflictingOccurrences += newConflicts;

        durationsSum += solvingAllocation.getDuration();

        return newConflicts > 0;
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
    public boolean updateAllocation(Occurrence toSolve, OccurrenceAllocation allocation) {
        this.removeAllocationOf(toSolve);
        return this.setAllocation(toSolve, allocation);
    }

    @Override
    public boolean iterate() {
        if (durationsSum >= maxDurationsSum && conflictingOccurrences == 0) {
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
    
    
    
}
