/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import net.personaltt.model.Occurrence;

/**
 *
 * @author docx
 */
public interface CostForOccurrence extends Cost {
    /**
     * Set occurrence which will be used in next cost computation for
     * reference for what occurrence is cost computet
     * @param occurrence 
     */
    public void setOccurrence(Occurrence occurrence);
}
