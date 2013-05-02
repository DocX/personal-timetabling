/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.core;

import javax.naming.OperationNotSupportedException;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.ReadablePeriod;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;

/**
 * Repeating interval domain. Represents boundless repetition of interval given
 * by starting reference in time continuum and partial (relative) duration. Period of 
 * repeating is also given as partial duration.
 * 
 * This structure finds for all points in the time continuum if is in domain or 
 * can retrieve set of intervals in given range.
 * @author docx
 */
public class RepeatingIntervalDomain implements IIntervalsTimeDomain {
     
    LocalDateTime referenceIntervalStart;
    
    BaseSingleFieldPeriod intervalDuration;
    
    BaseSingleFieldPeriod repeatingPeriod;

    public RepeatingIntervalDomain(LocalDateTime referenceIntervalStart, BaseSingleFieldPeriod intervalDuration, BaseSingleFieldPeriod repeatingPeriod) {
        this.referenceIntervalStart = referenceIntervalStart;
        this.intervalDuration = intervalDuration;
        this.repeatingPeriod = repeatingPeriod;
    }

    @Override
    public IntervalsSet getIntervalsIn(Interval i) {
        IntervalsSet set = new IntervalsSet();
        
        int periods = getNearestPeriodStartToAndBefore(i.start);
        
        Period currentPeriod = repeatingPeriod.toPeriod().multipliedBy(periods);
        while(true) {
            
            LocalDateTime intervalStart = referenceIntervalStart.plus(currentPeriod);
            LocalDateTime intervalEnd = intervalStart.plus(intervalDuration);
            
            // ending after range
            if (intervalEnd.isAfter(i.end)) {
                break;
            }
            
            
            set.unionWith(intervalStart, intervalEnd);
            
            currentPeriod = currentPeriod.plus(repeatingPeriod);
        }
        
        return set;
    }

    private int getNearestPeriodStartToAndBefore(LocalDateTime start) {
        // compute periods count difference from referenceStart to given start
        int periods = 0;
        switch(repeatingPeriod.getClass().getName()) {
            case "org.joda.time.Months":
                periods = Months.monthsBetween(referenceIntervalStart,start ).getMonths() / ((Months)(repeatingPeriod)).getMonths();
                break;
            case "org.joda.time.Days":
                periods = Days.daysBetween(referenceIntervalStart,start).getDays() / ((Days)(repeatingPeriod)).getDays();
                break;
            case "org.joda.time.Hours":
                periods = Hours.hoursBetween(referenceIntervalStart,start).getHours() / ((Hours)(repeatingPeriod)).getHours();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return periods < 0 ? periods  : periods;
    }
    
}
