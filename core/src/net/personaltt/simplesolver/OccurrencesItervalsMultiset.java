package net.personaltt.simplesolver;

import java.util.List;
import java.util.TreeMap;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.Schedule;
import org.joda.time.LocalDateTime;

/**
 * Occurrences Intervals Multiset structure contains multiset of intervals (overlapping intervals)
 * connected to occurrences allocation. It can find what occurrences are in given point or interval,
 * do set operations on it with other interval sets and can efficiently delete or add intervals.
 * @author docx
 */
public class OccurrencesItervalsMultiset {
    
    /**
     * Internal sorted structure for storing edges of itnervals and mapping them
     * to list of occurrences effective to the right of the edge
     */
    TreeMap<LocalDateTime, List<Occurrence>> edges;

    /**
     * Creates intervals set for occurrences in given schedule. For each occurrence
     * and its allocation stores one interval
     * @param workingSchedule 
     */
    OccurrencesItervalsMultiset(Schedule workingSchedule) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Retrieves conflicting occurrences in the set. It is set of occurrences
     * in the intervals that have more than one occurence
     * @return 
     */
    List<Occurrence> getConflictingOccurrences() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
  
    
}
