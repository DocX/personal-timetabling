/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.client;

import java.util.ArrayList;
import java.util.Iterator;
import net.personaltt.model.Occurrence;
import net.personaltt.model.ProblemDefinition;
import net.personaltt.timedomain.ConverterToInteger;
import net.personaltt.timedomain.IIntervalsTimeDomain;
import net.personaltt.timedomain.Interval;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;
import org.joda.time.LocalDateTime;

/**
 * Problem definition builder encapsulate ProblemDefinition and provides
 * methods for easy construction of it from client representation
 * @author docx
 */
public class ProblemDefinitionBuilder {

    ProblemDefinition definition;
    
    public ProblemDefinitionBuilder() {
        definition = new ProblemDefinition();
    }
    
    /**
     * Adds occurrence definition and values to problem definition.
     * It converts occurrence time domain from IIntervalsTimeDomain to BaseIntervalsSet
     * used in solver by converting LocalDateTime bounds to Integer values in seconds from 
     * Unix epoch. 
     * 
     * @param id
     * @param start
     * @param duration
     * @param minDuration
     * @param maxDuration
     * @param domain 
     */
    public void addOccurrence(int id, LocalDateTime start, int duration, int minDuration, int maxDuration, IIntervalsTimeDomain domain) {
        
        // converts timedomain intervals set to numeric domain
        Occurrence occurrence = new Occurrence(
                domain.getIntervalsIn(domain.getBoundingInterval()).toNumericIntervalsSet(), 
                minDuration, 
                maxDuration, 
                id
                );
        
        definition.addOccurrence(occurrence, new BaseInterval<>(
                new ConverterToInteger().convert(start),
                new ConverterToInteger().convert(start.plusSeconds(duration))
                ));
        
        System.out.println("Added event to problem builder");
    }
    
    /**
     * Set initial start preferrence priority. When 2 occurrences tries to get one 
     * allocation, occurrence with greater priority number should win.
     * @param id
     * @param priority 
     */
    public void setPreferredPriority(int id, int priority) {
        this.getOccurrence(id).setPreferrencePriority(priority);
    }
    

    /**
     * Returns definition as is constructed so far.
     * @return 
     */
    public ProblemDefinition getDefinition() {
        return definition;
    }
    
    /**
     * Crops actual occurrences's in builder domains to start from givin date.
     * If resulting domain contains some interval smaller than min duration, 
     * it removes it completely. If occurrence starts before resulting domain
     * lower bound, it moves it to lower bound.
     * @param from_date 
     * @return True if at least one domain has some time. False if no domain remain time
     * and thus no problem is defined
     */
    public boolean cropToFutureOf(LocalDateTime toDate) {
        int cropToDateNum = new ConverterToInteger().convert(toDate);
        
        ArrayList<Occurrence> emptyDomain = new ArrayList<>(); 
        
        for (Iterator<Occurrence> it = definition.problemOccurrences.iterator(); it.hasNext();) {
            Occurrence occurrence = it.next();
        
            BaseIntervalsSet<Integer> cropping = new BaseIntervalsSet<>();
            int domainUpper = occurrence.getDomain().getUpperBound();
            
            if (domainUpper <= cropToDateNum) {
                // remove entire domain - intersect with empty domain
            } else {
                // remove fdrom date
                cropping.unionWith(cropToDateNum, domainUpper);
            }
                        
            occurrence.getDomain().intersectWith(cropping);
            
            // check first domain interval if is smaller than minimal duration
            // only first could be cropped
            BaseInterval<Integer> first = occurrence.getDomain().getFirstInterval();
            if (first != null && first.getStart() == cropToDateNum && 
                    (first.getEnd() - first.getStart()) < occurrence.getMinDuration()) {
                // remove thatgolf karlovy varygolf karlovy vary interval
                occurrence.getDomain().minus(BaseIntervalsSet.oneInterval(first.getStart(), first.getEnd()));
            }
            
            // if domain is empty, remove occurrence
            if (occurrence.getDomain().empty()) {
               emptyDomain.add(occurrence);
            }
            
            
        }
        
        for (Occurrence occurrence : emptyDomain) {
            definition.removeOccurrence(occurrence);
        }
        
        return definition.problemOccurrences.isEmpty() == false;
    }
    
    private Occurrence getOccurrence(int id) {
        for (Occurrence occurrence : this.definition.problemOccurrences) {
            if (occurrence.getId() == id) {
                return occurrence;
            }
        }
        return null;
    }
    
    
//    /**
//     * Add ordering prefference of id_before should be before id_after
//     * @param id_before
//     * @param id_after 
//     */
//    public void addOrderPreferrence(int id_before, int id_after) {
//        // check for cycle
//    }
//    
//    /**
//     * Enum of supported linked periods. Each period means that the same
//     * value if that period is maintained for all occurrences in linked group.
//     */
//    public enum LinkedPeriods {
//        /**
//         * Same time in day. For example all at 14:30 anyday
//         */
//        TIME_IN_DAY,
//        /**
//         * Same hour in day and day in week. For example all at Thu 9:00
//         */
//        DAY_AND_TIME_IN_WEEK,
//        /**
//         * Same hour in day and date in month. For example all at 15th of month at 15:00
//         */
//        DAY_AND_TIME_IN_MONTH
//    }
//    
//    /**
//     * Set linked occurrences. Given periods, when occurrences are linked, 
//     * same differrence from start of period in which occurrence lay is
//     * enforced.
//     * One occurrence can be at least in one linked occurrences group.
//     * @param ids 
//     */
//    public void setLinkedOccurrences(int[] ids, LinkedPeriods period) {
//        //TODO
//        // check if ids is not in some linked group already
//        // convert period to list of start dates - for each occurrence in its domain boundaries
//        // 
//    }
    
}
