/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import java.util.List;
import java.util.Map;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.Schedule;
import net.personaltt.simplesolver.SolverSolution;
import net.personaltt.simplesolver.SolverState;
import net.personaltt.utils.IntervalMultimap;

/**
 * Simple solver solution. It uses intervals multimap as backing store of
 * allocations. Also keeps updated Schedule all time
 * @author docx
 */
public class SimpleSolverState implements SolverState {

    IntervalMultimap<Integer, Occurrence> allocationMultimap;
    
    Schedule solutionSchedule;
    
    int conflictingOccurrences;
    
    long durationsSum;
    long maxDurationsSum;
    
    @Override
    public void init(Schedule schedule) {
        allocationMultimap = new IntervalMultimap<>();
        solutionSchedule = (Schedule)schedule.deepClone();
        conflictingOccurrences = 0;
        durationsSum = 0;
        maxDurationsSum = 0;
        
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : solutionSchedule.getOccurrencesAllocations()) {
            int newConflicts = allocationMultimap.put(entry.getKey(), entry.getValue().toInterval());
            conflictingOccurrences += newConflicts;
            if (entry.getValue().getDuration() > entry.getKey().getMaxDuration()) {
                throw new IllegalArgumentException("Duration cannot be greater than max duration");
            }
            
            durationsSum += entry.getValue().getDuration();
            maxDurationsSum += entry.getKey().getMaxDuration();
        }
    }

    @Override
    public boolean terminationCondition() {
        return durationsSum >= maxDurationsSum && conflictingOccurrences == 0;
    }

    @Override
    public long cost() {
        //return conflictingOccurrences;
        return maxDurationsSum - durationsSum;
    }

    @Override
    public IntervalMultimap<Integer, Occurrence> allocationsMultimap() {
        return allocationMultimap;
    }

    @Override
    public OccurrenceAllocation removeAllocationOf(Occurrence toSolve) {
        int conflictsRemoved = allocationMultimap.remove(toSolve);
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

        int newConflicts = allocationMultimap.put(toSolve, solvingAllocation.toInterval());
        conflictingOccurrences += newConflicts;

        durationsSum += solvingAllocation.getDuration();

        return newConflicts > 0;
    }

    @Override
    public Schedule getSchedule() {
        return solutionSchedule;
    }

    @Override
    public SolverSolution cloneSolution() {
        final long cost = this.cost();
        final Schedule clonedSchedule = (Schedule)this.getSchedule().allocationsClone();
        
        return new SolverSolution() {
            
            @Override
            public long cost() {
                return cost;
            }

            @Override
            public Schedule getSchedule() {
                return clonedSchedule;
            }
        };
    }
    
    
    
}
