/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.simplesolver;

import java.util.Map;
import java.util.Random;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.Schedule;
import net.personaltt.utils.IntervalMultimap;

/**
 * Roulette selection selects occurrence with larger cost with more probability
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
        
        // roulete selection by occurrence cost
        
        Map<Occurrence, Integer> occurrencesCosts = schedule.valuesOverlappingSum(new IntervalMultimap.KeyDifference<Integer>() {

            @Override
            public int diff(Integer a, Integer b) {
                return a.intValue() - b.intValue();
            }
        });
        
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
