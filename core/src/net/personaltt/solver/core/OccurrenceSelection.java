/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.core;

import net.personaltt.model.Occurrence;

/**
 * Occurrence selection implements selection of occurrence to change in each iteration
 * @author docx
 */
public interface OccurrenceSelection {
    
    /**
     * Select occurrence to solve. 
     * It should select best occurrence that allocation is potentially largest
     * step towards optimal solution.
     * @param solution
     * @return 
     */
    Occurrence select(SolverState solution);
}
