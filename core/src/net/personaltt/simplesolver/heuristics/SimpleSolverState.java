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
    
    @Override
    public void init(Schedule schedule) {
        allocationMultimap = new IntervalMultimap<>();
        solutionSchedule = (Schedule)schedule.deepClone();
        conflictingOccurrences = 0;
        
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : solutionSchedule.getOccurrencesAllocations()) {
            int newConflicts = allocationMultimap.put(entry.getKey(), entry.getValue().toInterval());
            conflictingOccurrences += newConflicts;
        }
    }

    @Override
    public boolean terminationCondition() {
        return conflictingOccurrences == 0;
    }

    @Override
    public int cost() {
        return conflictingOccurrences;
    }

    @Override
    public IntervalMultimap<Integer, Occurrence> allocationsMultimap() {
        return allocationMultimap;
    }

    @Override
    public OccurrenceAllocation removeAllocationOf(Occurrence toSolve) {
        int conflictsRemoved = allocationMultimap.remove(toSolve);
        conflictingOccurrences -= conflictsRemoved;
        return solutionSchedule.getAllocationOf(toSolve);
    }

    @Override
    public boolean setAllocation(Occurrence toSolve, OccurrenceAllocation solvingAllocation) {
       solutionSchedule.getAllocationOf(toSolve).set(solvingAllocation);
       
       int newConflicts = allocationMultimap.put(toSolve, solvingAllocation.toInterval());
       conflictingOccurrences += newConflicts;
       return newConflicts > 0;
    }

    @Override
    public Schedule getSchedule() {
        return solutionSchedule;
    }

    @Override
    public SolverSolution cloneSolution() {
        final int cost = this.cost();
        final Schedule clonedSchedule = (Schedule)this.getSchedule().allocationsClone();
        
        return new SolverSolution() {
            
            @Override
            public int cost() {
                return cost;
            }

            @Override
            public Schedule getSchedule() {
                return clonedSchedule;
            }
        };
    }
    
    
    
}
