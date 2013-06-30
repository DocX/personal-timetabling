/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import java.util.ArrayList;
import org.joda.time.LocalDateTime;

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
    
    @Override
    public boolean isBounded() {
        // stack is bounded iff 
        // has no add action of unbounded domain above nearest bounded mask or add action
        
        // empty set is bounded
        boolean bounded = true;
        for (ActionBase actionBase : actionStack) {
            bounded = actionBase.boundedAppliedOn(bounded);
        }
        
        return bounded;
    }

    @Override
    public Interval getBoundingInterval() {
        Interval boundary = null;
        
        // go from ground and build bounding
        // addition of bounded set adds to boundary
        // mask of bounded set intersect boundary
        // removing of bounded set can make boundary smaller, but it will still be boundary
        //  so do nothing
        //
        
        // first make sure that this is bounded
        if (this.isBounded() == false) {
            return null;
        }
        
        for (ActionBase actionBase : actionStack) {
            boundary = actionBase.applyOnBoundary(boundary);
        }
        
        return boundary;
    }    

    @Override
    public boolean intersects(IIntervalsTimeDomain other) {
        return IntervalsTimeDomainUtils.genericIntersects(this, other);
    }
    
    
    /* actions implementation */
    
    abstract class ActionBase {
        IIntervalsTimeDomain domain;

        public ActionBase(IIntervalsTimeDomain domain) {
            this.domain = domain;
        }
        
        // aplies action on given interval set in the given range
        abstract void applyOn(IntervalsSet s, Interval range);
        
        // return true if result of applyon of other on bounded will be bounded
        abstract boolean boundedAppliedOn(boolean groundBounded);
        
        // apply action operand boundary to given boundary using 
        // action operation and return boundary of result
        // Return null if action operand is not bounded
        abstract Interval applyOnBoundary(Interval boundary);
    }
    
    /**
     * Add action represents union operation of its domain to another
     */
    class AddAction extends ActionBase {

        public AddAction(IIntervalsTimeDomain domain) {
            super(domain);
        }
        
        @Override
        void applyOn(IntervalsSet s, Interval range) {
            IntervalsSet i = domain.getIntervalsIn(range);
            s.unionWith(i);
        }

        @Override
        boolean boundedAppliedOn(boolean otherBounded) {
            boolean thisBounded = domain.isBounded();
            return thisBounded && otherBounded;
        }

        @Override
        Interval applyOnBoundary(Interval boundary) {
            if (!domain.isBounded()) {
                return null;
            }
                
            // extends boundary by this domain boundary
            Interval thisBoundary = this.domain.getBoundingInterval();
            
            if (boundary == null) {
                return thisBoundary;
            }
            else {
                return new Interval(
                    thisBoundary.getStart().isBefore(boundary.getStart()) ? thisBoundary.getStart() : boundary.getStart(),
                    thisBoundary.getEnd().isAfter(boundary.getEnd()) ? thisBoundary.getEnd() : boundary.getEnd()
                    );
            }
        }
    }

    /**
     * Remove action represents minus set operation
     */
    class RemoveAction extends ActionBase {

        public RemoveAction(IIntervalsTimeDomain domain) {
            super(domain);
        }
        
        
        @Override
        void applyOn(IntervalsSet s, Interval range) {
            IntervalsSet i = domain.getIntervalsIn(range);
            s.minus(i);
        }
        
        @Override
        boolean boundedAppliedOn(boolean otherBounded) {
            return otherBounded;
        }
        
        @Override
        Interval applyOnBoundary(Interval boundary) {
            // remove can only make boundary smaller,
            // so larger boundary is still boundary
            // return the same
            
            return boundary;
        }
    }
    
    /**
     * Mask action representes intersection set operation
     */
    class MaskAction extends ActionBase {

        public MaskAction(IIntervalsTimeDomain domain) {
            super(domain);
        }
        
        
        @Override
        void applyOn(IntervalsSet s, Interval range) {
            IntervalsSet i = domain.getIntervalsIn(range);
            s.intersectWith(i);
        }
        
        @Override
        boolean boundedAppliedOn(boolean otherBounded) {
            boolean thisBounded = domain.isBounded();
            return thisBounded || otherBounded;
        }
        
        @Override
        Interval applyOnBoundary(Interval boundary) {
            // mask boundary crops given boundary if bounded
            
            if(this.domain.isBounded() == false) {
                return boundary;
            }
            
            // extends boundary by this domain boundary
            Interval thisBoundary = this.domain.getBoundingInterval();
            
            if (boundary == null) {
                return thisBoundary;
            }
            else {
                // crops given boundary using this boundary
                return new Interval(
                    thisBoundary.getStart().isAfter(boundary.getStart()) ? thisBoundary.getStart() : boundary.getStart(),
                    thisBoundary.getEnd().isBefore(boundary.getEnd()) ? thisBoundary.getEnd() : boundary.getEnd()
                    );
            }
            
        }
    }
    
}
