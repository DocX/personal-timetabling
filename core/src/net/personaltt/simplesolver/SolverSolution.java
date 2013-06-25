/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import net.personaltt.problem.Schedule;

/**
 * Solver solution
 * @author docx
 */
public interface SolverSolution {
    /**
     * Returns current cost
     * @return 
     */
    public long cost();
    
    
    /**
     * Returns schedule of current solution.
     * @return 
     */
    public Schedule getSchedule();
}
