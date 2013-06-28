/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.core;

import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.model.Schedule;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;

/**
 * Solver state keeps data in solver run and provides subroutines for solver
 * algorithm.
 * @author docx
 */
public interface SolverState extends SolverSolution, Comparable {

    public void init(Schedule schedule);

    /**
     * Return true if solution is acceptable
     * @return 
     */
    public boolean terminationCondition();

    /**
     * Returns current solutions allocation multimap
     * @return 
     */
    public IntervalMultimap<Occurrence> allocationsMultimap();

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
     * Updates allocation of occurrence. If occurrence is not allocated, allocates it.
     * Is it is allocated, updates its allocation.
     * In both cases, updates inner state of costs etc.
     * If new allocation is conflicting with actual schedule, true is returned.
     * @param toSolve
     * @param allocation
     * @return 
     */
    public boolean updateAllocation(Occurrence toSolve, OccurrenceAllocation allocation);
    
    /**
     * Returns object with cloned solution
     * @return 
     */
    public SolverSolution cloneSolution();
    
    /**
     * Return object with cost, but not need to clone solution
     * @return 
     */
    public SolverSolution cloneCost();
    
}
