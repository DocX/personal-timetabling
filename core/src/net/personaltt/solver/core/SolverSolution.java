/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.core;

import net.personaltt.model.Schedule;

/**
 * Solver solution
 * @author docx
 */
public interface SolverSolution {
    /**
     * Cost of solution againts optimal solution
     * @return 
     */
    public long preferenceCost();
    
    /**
     * Cost of not satisfied constraints
     * @return 
     */
    public long constraintsCost();
    
    /**
     * Returns schedule of current solution.
     * @return 
     */
    public Schedule getSchedule();
}
