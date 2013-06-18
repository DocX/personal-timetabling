/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver.heuristics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.Schedule;
import net.personaltt.simplesolver.OccurrenceSelection;
import net.personaltt.utils.IntervalMultimap;
import net.personaltt.utils.ValuedInterval;

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
        
        // map of cost of occurrences.
        Map<Occurrence, Long> occurrencesCosts = new HashMap<>();
        
        // for all occurrences in multimap, walk values in interval of occurrence and add 
        // cost
        for (Occurrence valuesInterval : schedule.values()) {
            long cost = 0;
            for (ValuedInterval<Integer, List<Occurrence>> overlapping : schedule.intervalsIn(valuesInterval)) {
                // length of interval times number of other overlapping values
                cost += (overlapping.getEnd() - overlapping.getStart()) * (overlapping.getValues().size() - 1);
            }
            occurrencesCosts.put(valuesInterval, cost);
        }
        
        // sum costs
        long costSum = 0;
        for (Map.Entry<Occurrence, Long> entry : occurrencesCosts.entrySet()) {
            costSum += entry.getValue();
        }
        
        // get random in sum
        long selection = nextLong(random, costSum);
        
        // go throught occurrences and first where sum is less than random
        costSum = 0;
        Occurrence selectedOccurrence = null;
        for (Map.Entry<Occurrence, Long> entry : occurrencesCosts.entrySet()) {
            if (costSum > selection) {
                break;
            }
            selectedOccurrence = entry.getKey();
            costSum += entry.getValue();
        }
        
        return selectedOccurrence;
    }
    
    long nextLong(Random rng, long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }

        long bits, val;
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }
    
}
