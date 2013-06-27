/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import cern.colt.function.IntDoubleProcedure;
import cern.colt.map.OpenIntDoubleHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.Schedule;
import net.personaltt.simplesolver.OccurrenceSelection;
import net.personaltt.utils.IntervalMultimap;
import net.personaltt.utils.RandomUtils;
import net.personaltt.utils.ValuedInterval;
import org.omg.CORBA.DynAnyPackage.Invalid;

/**
 * Roulette occurrence selection. Selects occurrence with larger cost with more probability
 * cost is sum of lengths shared with occurrence for each of other occurrences allocations
 * @author docx
 */
public class RouletteSelection implements OccurrenceSelection {

    Random random = new Random();   
    
     /**
     * Gets occurrence to solve using heuristic from conflicting occurrences with
     * cost
     * @param conflictingOccurrencesWithCost
     * @return 
     */    
    @Override
    public Occurrence select(IntervalMultimap<Integer, Occurrence> schedule) {
        
        // map of costs of occurrences
        cern.colt.map.OpenIntDoubleHashMap occurrencesCosts = new OpenIntDoubleHashMap(schedule.size()*2);
        
        long costSum = 0;
      
        // Sum cost for each occurrence. Walk throught all intervals chunks in map
        // and sum number of values in chunk to each value in chunk, which is occurrence
        for (ValuedInterval<Integer, List<Occurrence>> valuesInterval : schedule.valuesIntervals()) {
            for (Occurrence occurrence : valuesInterval.getValues()) {
                double oldSum = occurrencesCosts.get(occurrence.getId());
                // add value of interval length multiplied by count of other occurrences filling that interval
                long cost =  
                        (valuesInterval.getEnd() - valuesInterval.getStart()) *
                        (valuesInterval.getValues().size() - 1);
                costSum += cost;
                
                occurrencesCosts.put(occurrence.getId(), oldSum + cost);
            }
        }
        
        // add cost for smaller duration
        
        for (Occurrence occurrence : schedule.keys()) {
            double old = occurrencesCosts.get(occurrence.getId());
            long durCost =  (long)(occurrence.getMaxDuration() - occurrence.getAllocation().getDuration());
            // add one to each occurrence to ensure all have at least 1/n probabilty of selection
            durCost += 1;
            costSum += durCost ;
            occurrencesCosts.put(occurrence.getId(), old + durCost);
        }
        
        // get random in sum
        long selection = RandomUtils.nextLong(random, costSum);
        
        // go throught occurrences and first where sum is less than random
        SelectionFunc select = new SelectionFunc(selection);
        occurrencesCosts.forEachPair(select);
        
        for (Occurrence occurrence : schedule.keys()) {
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
            
            if (costSum > selection) {
                return false;
            }
            selectedOccurrenceId = i;
            costSum += d;
            return true;
        }
    };
    
}
