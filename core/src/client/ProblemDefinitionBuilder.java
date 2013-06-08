/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import net.personaltt.problem.Occurrence;
import net.personaltt.problem.ProblemDefinition;
import net.personaltt.timedomain.ConverterToInteger;
import net.personaltt.timedomain.IIntervalsTimeDomain;
import net.personaltt.utils.BaseInterval;
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
    }

    /**
     * Returns definition as is constructed so far.
     * @return 
     */
    public ProblemDefinition getDefinition() {
        return definition;
    }
    
}
