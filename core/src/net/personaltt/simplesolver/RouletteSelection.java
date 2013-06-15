/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.IntervalMultimap;

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
        
        // map of 
        Map<Occurrence, Integer> occurrencesCosts = new HashMap<>();
      
        for (IntervalMultimap<Integer, Occurrence>.ValuesInterval valuesInterval : schedule.valuesIntervals()) {
            for (Occurrence occurrence : valuesInterval.getValues()) {
                Integer oldSum = occurrencesCosts.get(occurrence);
                // add value of interval length multiplied by count of other occurrences filling that interval
                int newSum = (oldSum == null ? 0 : oldSum) + 
                        (valuesInterval.getInterval().getEnd() - valuesInterval.getInterval().getStart()) *
                        (valuesInterval.getValues().size() - 1);
                
                occurrencesCosts.put(occurrence, newSum);
            }
        }
        
        // sum costs
        int costSum = 0;
        for (Map.Entry<Occurrence, Integer> entry : occurrencesCosts.entrySet()) {
            costSum += entry.getValue();
        }
        
        // get random in sum
        int selection = random.nextInt(costSum);
        // go throught occurrences and first where sum is less than random
        costSum = 0;
        Occurrence selectedOccurrence = null;
        for (Map.Entry<Occurrence, Integer> entry : occurrencesCosts.entrySet()) {
            if (costSum > selection) {
                break;
            }
            selectedOccurrence = entry.getKey();
            costSum += entry.getValue();
        }
        
        return selectedOccurrence;
    }
    
}
