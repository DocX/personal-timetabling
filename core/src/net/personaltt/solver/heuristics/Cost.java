/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import net.personaltt.utils.BaseInterval;

/**
 *
 * @author docx
 */
public interface Cost {
 
     public long computeCostOfAllocation(BaseInterval<Integer> allocation);
}
