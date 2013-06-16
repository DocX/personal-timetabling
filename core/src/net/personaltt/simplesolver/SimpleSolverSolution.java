/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import java.util.List;
import java.util.Map;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.IntervalMultimap;

/**
 * Simple solver solution. It uses intervals multimap as backing store of
 * allocations. Also keeps updated Schedule all time
 * @author docx
 */
public class SimpleSolverSolution implements SolverSolution {

    IntervalMultimap<Integer, Occurrence> allocationMultimap;
    
    Schedule solutionSchedule;
    
    int conflictingOccurrences;
    
    @Override
    public void init(Schedule schedule) {
        allocationMultimap = new IntervalMultimap<>();
        solutionSchedule = (Schedule)schedule.clone();
        conflictingOccurrences = 0;
        
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : schedule.getOccurrencesAllocations()) {
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
    
}