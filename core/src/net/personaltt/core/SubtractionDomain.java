package net.personaltt.core;

/**
 * Subtraction domain. It removes intervals of one IIntervalsTimeDomain from another IIntervalsTimeDomain
 * @author docx
 */
public class SubtractionDomain implements IIntervalsTimeDomain {
    IIntervalsTimeDomain minuend;
    
    IIntervalsTimeDomain subtrahend;

    public SubtractionDomain(IIntervalsTimeDomain minuend, IIntervalsTimeDomain subtrahend) {
        this.minuend = minuend;
        this.subtrahend = subtrahend;
    }

    @Override
    public IntervalsSet getIntervalsIn(Interval i) {
        IntervalsSet minuend_set = minuend.getIntervalsIn(i);
        IntervalsSet subtrahend_set = subtrahend.getIntervalsIn(i);
        
        minuend_set.minus(subtrahend_set);
        return minuend_set;
    }
    
    
}
