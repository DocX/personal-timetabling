/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import java.util.ArrayList;

/**
 * Action stack domain represents domain as a stack of other IntervalsTimeDomain 
 * with interval operation applied to the stack bellow it. At the bottom of stack
 * is empty set.
 * 
 * Its alternative implementation to binary expression trees nodes (Union,Subtraction,Intersection Domain classes)
 * @author docx
 */
public class ActionStackDomain implements IIntervalsTimeDomain {

    private ArrayList<ActionBase> actionStack;

    public ActionStackDomain() {
        actionStack = new ArrayList<>();
    }
    
    @Override
    public IntervalsSet getIntervalsIn(Interval i) {
        IntervalsSet set = new IntervalsSet();
        
        // go throu stack and build intervals set
        for (ActionBase action : actionStack) {
            action.applyOn(set, i);
        }
        
        return set;
    }
    
    static final int ADD = 1;
    static final int REMOVE = 2;
    static final int MASK = 3;
    
    /**
     * Pushes time domain with action to the top of stack.
     * Resulting set will be computed as "(current_stack) *action* domain"
     * @param a
     * @param domain 
     */
    public void push(int action, IIntervalsTimeDomain domain) {
        switch (action) {
            case ADD:
                actionStack.add(new AddAction(domain));
                break;
            case REMOVE:
                actionStack.add(new RemoveAction(domain));
                break;
            case MASK:
                actionStack.add(new MaskAction(domain));
                break;
        }
    }
    
    /* actions implementation */
    
    abstract class ActionBase {
        IIntervalsTimeDomain domain;

        public ActionBase(IIntervalsTimeDomain domain) {
            this.domain = domain;
        }
        
        // aplies action on given interval set in the given range
        abstract void applyOn(IntervalsSet s, Interval range);
    }
    
    class AddAction extends ActionBase {

        public AddAction(IIntervalsTimeDomain domain) {
            super(domain);
        }
        
        @Override
        void applyOn(IntervalsSet s, Interval range) {
            IntervalsSet i = domain.getIntervalsIn(range);
            s.unionWith(i);
        }
    }

    class RemoveAction extends ActionBase {

        public RemoveAction(IIntervalsTimeDomain domain) {
            super(domain);
        }
        
        
        @Override
        void applyOn(IntervalsSet s, Interval range) {
            IntervalsSet i = domain.getIntervalsIn(range);
            s.minus(i);
        }
    }
    class MaskAction extends ActionBase {

        public MaskAction(IIntervalsTimeDomain domain) {
            super(domain);
        }
        
        
        @Override
        void applyOn(IntervalsSet s, Interval range) {
            IntervalsSet i = domain.getIntervalsIn(range);
            s.intersectWith(i);
        }
    }
    
 
    
}
