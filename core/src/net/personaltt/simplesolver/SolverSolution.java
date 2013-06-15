/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.IntervalMultimap;

/**
 * Solver solution keeps data in solver run and provides subroutines for solver
 * algorithm.
 * @author docx
 */
public interface SolverSolution {

    public void init(Schedule schedule);

    /**
     * Return true if solution is acceptable
     * @return 
     */
    public boolean terminationCondition();
    
    /**
     * Returns current cost
     * @return 
     */
    public int cost();

    /**
     * Returns current solutions allocation multimap
     * @return 
     */
    public IntervalMultimap<Integer, Occurrence> allocationsMultimap();

    /**
     * Removes allocation of occurrence from solution and returns it 
     * @param toSolve
     * @return 
     */
    public OccurrenceAllocation removeAllocationOf(Occurrence toSolve);

    /**
     * Set occurrence allocation. If occurrence has not allocated in solution, allocation is 
     * created
     * @param toSolve
     * @param solvingAllocation
     * @return 
     */
    public boolean setAllocation(Occurrence toSolve, OccurrenceAllocation solvingAllocation);

    /**
     * Returns schedule of current solution.
     * @return 
     */
    public Schedule getSchedule();
    
}
