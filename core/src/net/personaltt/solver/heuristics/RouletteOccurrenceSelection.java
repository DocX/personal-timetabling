/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.solver.heuristics;

import cern.colt.function.IntDoubleProcedure;
import cern.colt.map.OpenIntDoubleHashMap;
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
        
        // map of costs of occurrences
        cern.colt.map.OpenIntDoubleHashMap occurrencesCosts = new OpenIntDoubleHashMap(solution.allocationsMultimap().size()*2);
        
        long costSum = 0;
        // Sum cost for each occurrence. Walk throught all intervals chunks in map
        // and sum number of values in chunk to each value in chunk, which is occurrence
        for (ValuedInterval<Integer, List<Occurrence>> valuesInterval : solution.allocationsMultimap().valuesIntervals()) {
            for (Occurrence occurrence : valuesInterval.getValues()) {
                double oldSum = occurrencesCosts.get(occurrence.getId());
                // add value of interval length multiplied by count of other occurrences filling that interval
                long cost =  
                        (valuesInterval.getEnd() - valuesInterval.getStart()) *
                        (valuesInterval.getValues().size() - 1)
                        ;
                
                costSum += cost;
                
                occurrencesCosts.put(occurrence.getId(), oldSum + cost);
            }
        }
        
        long allocationCostWeight = costSum == 0 ? 1 : 0;
        
        // compute total sum of costs, power cost to make exponential scale of cost
        costSum = 0;
        for (Occurrence occurrence : solution.allocationsMultimap().keys()) {
            double old = occurrencesCosts.get(occurrence.getId());
            double newCost = old * old * old + (allocationCostWeight * (occurrence.getAllocationCost() + 1));
            
            costSum += newCost;
            occurrencesCosts.put(occurrence.getId(), newCost);
        }
        
        
        // get random in sum
        long selection = RandomUtils.nextLong(random, costSum);
        
        // go throught occurrences and first where sum is less than random
        SelectionFunc select = new SelectionFunc(selection);
        occurrencesCosts.forEachPair(select);
        
        for (Occurrence occurrence : solution.allocationsMultimap().keys()) {
            if (occurrence.getId() == select.selectedOccurrenceId) {
                return occurrence;
            }
        }
        throw new IllegalStateException();
    }
    
    private class SelectionFunc implements IntDoubleProcedure {
        long costSum = 0;
        int selectedOccurrenceId = 0;
        long selection;

        public SelectionFunc(long selection) {
            this.selection = selection;
        }

        @Override
        public boolean apply(int i, double d) {
            if (d == 0) {
                return true;
            }
            
            selectedOccurrenceId = i;
            costSum += d;
            
            if (costSum > selection) {
                return false;
            }
            
            return true;
        }
    };
    
}
