/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Period;
import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.ReadablePeriod;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.joda.time.PeriodType;

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

    /**
     * Generate interval set with all intervals intersecting given interval i
     * If repeat period is month, only intervals where the same day of month as
     * reference is possible si added.
     * @param i
     * @return 
     */
    @Override
    public IntervalsSet getIntervalsIn(Interval i) {
        IntervalsSet set = new IntervalsSet();
        
        // if period is monthly, add only intervals where same day  of month
        // as in reference date is possible in given month.
        boolean checkDayInMonth = repeatingPeriod.getPeriodType() == PeriodType.months();
        
        // get initial periods number from reference 
        // which is the latest period start before range
        int periods = getNearestPeriodStartToAndBefore(i.getStart());      
        LocalDateTime intervalStart = referenceIntervalStart.plus(repeatingPeriod.toPeriod().multipliedBy(periods));
        LocalDateTime intervalEnd = intervalStart.plus(intervalDuration);     
        
        // if first interval do not cross range border, move to the next,
        // which must be after start due to current is tha latest period before start
        if (intervalStart.plus(intervalDuration).isBefore(i.getStart())) {
            periods++;
            intervalStart = referenceIntervalStart.plus(repeatingPeriod.toPeriod().multipliedBy(periods));
            intervalEnd = intervalStart.plus(intervalDuration);     
        }
        
        while(!intervalStart.isAfter(i.getEnd())) {

            // if start do not match reference start and repeating pattern
            // ie if repeating each month with reference on 31st day of month,
            // months that have less than 31 days are not added
            
            // so add interval only if not deviate condition
            if (!checkDayInMonth || (intervalStart.getDayOfMonth() == referenceIntervalStart.getDayOfMonth())) {
                set.unionWith(intervalStart, intervalEnd);
            } 
            
            periods += 1;
            intervalStart = referenceIntervalStart.plus(repeatingPeriod.toPeriod().multipliedBy(periods));            
            intervalEnd = intervalStart.plus(intervalDuration);    
        
        }
        
        return set;
    }

    /**
     * Returs largest number of periods that when added to reference start time is 
     * before given start time
     * @param start
     * @return 
     */
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
    
    /**
     * Generate list of intervals of count periods referencing given start
     * @param start start date of first period
     * @param period partial duration of period
     * @param count count of periods to generate
     * @return List of intervals for each period.
     */
    public static List<Interval> periodsIntervals(LocalDateTime start, BaseSingleFieldPeriod period, int count) {
        
        ArrayList<Interval> periods = new ArrayList<>();
        
        Period p = period.toPeriod();
        
        for (int i = 0; i < count; i++) {
            periods.add(new Interval(
                    start.plus(p.multipliedBy(i)),
                    start.plus(p.multipliedBy(i+1))
                    ));
        }
        
        return periods;
        
    }

    /**
     * Determine if is bounded. Due to fundamentals of this domain is always false.
     * @return 
     */
    @Override
    public boolean isBounded() {
        return false;
    }

    /**
     * Gets bounding interval for this domain. Due to fundamentals of this domain,
     * thus it is boundless, null is always returned.
     * @return 
     */
    @Override
    public Interval getBoundingInterval() {
        return null;
    }

    @Override
    public boolean intersects(IIntervalsTimeDomain other) {
        return IntervalsTimeDomainUtils.genericIntersects(this, other);
    }
    
    
    
}
