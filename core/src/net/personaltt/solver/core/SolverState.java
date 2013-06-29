/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.core;

import java.util.List;
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
     * Enter next iteration. Return false solver should end
     * @return 
     */
    public boolean iterate();

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
    public OccurrenceAllocation unassigneAllocation(Occurrence toSolve);

    /**
     * Set occurrence allocation. If occurrence has not allocated in solution, allocation is 
     * created, returing list of occurrences conflicting with adding allocation
     * @param toSolve
     * @param solvingAllocation
     * @return 
     */
    public List<Occurrence> assigneAllocation(Occurrence toSolve, OccurrenceAllocation solvingAllocation);

    /**
     * Updates allocation of occurrence. If occurrence is not allocated, allocates it.
     * Is it is allocated, updates its allocation.
     * In both cases, updates inner state of costs etc.
     * If new allocation is conflicting with actual schedule, true is returned.
     * @param toSolve
     * @param allocation
     * @return 
     */
    public boolean setAllocation(Occurrence toSolve, OccurrenceAllocation allocation);
    
    /**
     * Return true if current solution is better than best solution.
     * @return 
     */
    public boolean isBetterThanBest();
    
    /**
     * Stores current solution as best
     */
    public void saveBestSolution();
    
    /**
     * Returns iteration number of last best solution. If no best solution was stored until now, 
     * current iteration number is returned;
     * @return 
     */
    public int getLastBestIteration();
    
    /**
     * Return current iteration number;
     * @return 
     */
    public int getItearation();
    
    /**
     * Returns stored best solution
     * @return 
     */
    public SolverSolution getBestSolution();
    
    /**
     * Return list of unassigned occurrences
     * @return 
     */
    public List<Occurrence> getUnassignedOccurrences();
}
