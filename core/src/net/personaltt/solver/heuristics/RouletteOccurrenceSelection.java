/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import cern.colt.function.IntDoubleProcedure;
import cern.colt.map.OpenIntDoubleHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.personaltt.model.Occurrence;
import net.personaltt.model.Schedule;
import net.personaltt.solver.core.OccurrenceSelection;
import net.personaltt.solver.core.SolverState;
import net.personaltt.utils.intervalmultimap.IntervalMultimap;
import net.personaltt.utils.RandomUtils;
import net.personaltt.utils.ValuedInterval;

/**
 * Roulette occurrence selection. Selects occurrence with larger cost with more probability
 * cost is sum of lengths shared with occurrence for each of other occurrences allocations
 * @author docx
 */
public class RouletteOccurrenceSelection implements OccurrenceSelection {

    Random random = new Random();   
    
     /**
     * Gets occurrence to solve using heuristic from conflicting occurrences with
     * cost
     * @param conflictingOccurrencesWithCost
     * @return 
     */    
    @Override
    public Occurrence select(SolverState solution) {
        // select random unassigned
        if (solution.getUnassignedOccurrences().size() > 0) {
            return solution.getUnassignedOccurrences().get(random.nextInt(solution.getUnassignedOccurrences().size()));
        }
        
        // get costs sum
        long totalCost = 0;
        for (Occurrence occurrence : solution.allocationsMultimap().keys()) {
            totalCost += occurrence.getAllocationCost();
        }
        
        if (totalCost == 0) {
            return null;
        }
            
        // get random in sum
        long selection = RandomUtils.nextLong(random, totalCost);
        
        // go throught occurrences and first where sum is less than random
        totalCost = 0;
        for (Occurrence occurrence : solution.allocationsMultimap().keys()) {
            totalCost += occurrence.getAllocationCost();
            if (totalCost > selection) {
                return occurrence;
            }
        }
        throw new IllegalStateException();
    }
    
    
}
