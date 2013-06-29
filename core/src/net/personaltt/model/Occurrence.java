package net.personaltt.model;

import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;

/**
 * Occurrence represents definition of one time quantum in the time axis. It means its 
 * time domain and ID. Current allocation will be held separately
 * @author docx
 */
public class Occurrence {
    
    /**
     * Intervals set representing occurrence's domain of what area on time axis it
     * can cover
     */
    BaseIntervalsSet<Integer> domain;
    
    /*
     * Lower bound of duration interval
     */
    int minDuration;
    
    /**
     * Upper bound of duration interval
     */
    int maxDuration;
    
    /**
     * ID of occurrence used to associate with other parts of algorithm and client
     */
    int id;
    
    /**
     * Reference to assigned allocation
     */
    OccurrenceAllocation allocation;
    
    /**
     * Preferred starting point
     */
    int preferredStart;
    
    /**
     * Priority of perturbing value of this occurrence. 
     * Greater value means higher priority of keeping unchanged.
     */
    int preferredWeight = 0;
    
    long domainLowerBound = 0;
    
    long domainUpperBound = 0;
    
    private long maximalPreferredStartDiff = 0;
    
    long domainLowerBound = 0;
    
    long domainUpperBound = 0;
    
    private long maximalPreferredStartDiff = 0;
    
    /**
     * 
     * @param domain
     * @param minDuration
     * @param maxDuration
     * @param id 
     */
    public Occurrence(BaseIntervalsSet<Integer> domain, int minDuration, int maxDuration, int id) {
        this.domain = domain;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.id = id;
        
        // get domain bounds to compute maximal difference from preferredStart
        initDomainBounds();
    }

    private void initDomainBounds() {
        if (domain == null) {
            return;
        }
        domainLowerBound = domain.getLowerBound();
        domainUpperBound = domain.getUpperBound();
    }
    
    /**
     * Occurrences are equal iff their ids are equal
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Occurrence) {
            return this.id == ((Occurrence)(obj)).id;
        }
        
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.id;
        return hash;
    }
    
    /**
     * Domain of occurrence. Set of intervals where can be occurrence allocated
     * @return 
     */
    public BaseIntervalsSet<Integer> getDomain() {
        return domain;
    }

    /**
     * Maximum duration of allocation of occurrence.
     * @return 
     */
    public int getMaxDuration() {
        return maxDuration;
    }

    /**
     * Minimum duration of allocation of occurrence
     * @return 
     */
    public int getMinDuration() {
        return minDuration;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
    
    /**
     * ID of occurrence.
     * @return 
     */
    public int getId() {
        return id;
    }

    /**
     * Shallow clone of occurrence. Since all fields of occurrence are atomic 
     * their are copied. Only domain is referenced to source. Allocation is not copied.
     * @return 
     */
    @Override
    public Object clone() {
        Occurrence o = new Occurrence(domain, minDuration, maxDuration, id);
        o.preferredWeight = this.preferredWeight;
        o.preferredStart = this.preferredStart;
        o.maximalPreferredStartDiff = this.maximalPreferredStartDiff;
        return o;
    }

    public OccurrenceAllocation getAllocation() {
        return allocation;
    }

    public void setAllocation(OccurrenceAllocation allocation) {
        this.allocation = allocation;
    }
    
    public void setInitialAllocation(OccurrenceAllocation allocation) {
        this.allocation = allocation;
        this.setPreferredStart(allocation.start);
    }
    
    private void setPreferredStart(int start) {
        this.preferredStart = start;
        this.maximalPreferredStartDiff = Math.max(preferredStart - domainLowerBound, domainUpperBound - preferredStart);
    }

    public int getPreferredWeight() {
        return preferredWeight;
    }

    public int getPreferredStart() {
        return preferredStart;
    }
    
    /**
     * Allocation cost is made primarly 
     * @return 
     */
    public long getAllocationCost() {
        if (allocation == null) {
            return 0;
        }
        return getAllocationCost(allocation.start, allocation.duration);
    }
    
    /**
     * Cost of given allocation for this occurrence. 
     * @param inAllocation
     * @return 
     */
    public long getAllocationCost(OccurrenceAllocation inAllocation) {
        if (inAllocation == null) {
            return 0;
        }
        return getAllocationCost(inAllocation.start, inAllocation.duration);
    }
    
    public long getAllocationCost(BaseInterval<Integer> allocationInterval) {
        if (allocationInterval == null) {
            return 0;
        }
        return getAllocationCost(allocationInterval.getStart(), allocationInterval.getEnd() - allocationInterval.getStart());
    }
    
    
    public long getAllocationCost(int start, int duration) {
        return (maxDuration - duration) * (maximalPreferredStartDiff) +  Math.abs(start - preferredStart);
    }
    
    public long getDurationCost() {
        if (allocation == null) {
            return 0;
        }
        return getDurationCost(allocation.duration);
    }
       
    public long getDurationCost(int duration) {
        return maxDuration - duration;
    }
    
    public long getPreferredStartCost() {
        if (allocation == null) {
            return 0;
        }
        return getPreferredStartCost(allocation.start);
    }
    
    public long getPreferredStartCost(long start) {
        return Math.abs(start - preferredStart); 
    }
}
