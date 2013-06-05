/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

/**
 *
 * @author docx
 */
public class UnionDomain implements IIntervalsTimeDomain {
    
    private IIntervalsTimeDomain first;
    
    private IIntervalsTimeDomain second;

    public UnionDomain(IIntervalsTimeDomain first, IIntervalsTimeDomain second) {
        this.first = first;
        this.second = second;
    }
    
    @Override
    public IntervalsSet getIntervalsIn(Interval i) {
        IntervalsSet first_set = first.getIntervalsIn(i);
        IntervalsSet second_set = second.getIntervalsIn(i);
        
        first_set.unionWith(second_set);
        return first_set;
    }
    
}
