/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.management.Query;
import net.personaltt.utils.IntMatrix;

/**
 * Set of utilitary methods for IntervalsTimeDomains
 * @author docx
 */
public class IntervalsTimeDomainUtils {
    
    /**
     * Compute matrix of "intersects" relation between given domains. 
     * @param domains
     * @param inRange
     * @return 
     */
    public static IntMatrix computeIntersectionMatrix(List<IIntervalsTimeDomain> domains) {
        
        // create matrix
        IntMatrix matrix = new IntMatrix(domains.size());
        
        // for each pair test if intersecting
        for (int i = 0; i < domains.size(); i++) {
            matrix.set(i, i, 0);
            for (int j = i+1; j < domains.size(); j++) {
                boolean intersects = domains.get(i).intersects(domains.get(j));
                matrix.set(i, j, intersects ? 1 : 0);
                matrix.set(j, i, intersects ? 1 : 0);
            }
        }
        
        return matrix;
    }
    
    /**
     * Determine if two domains intersects in generic way. Simple implementation
     * of intersection detection when domains are converted to intervals set.
     * @param a
     * @param b
     * @return 
     */
    public static boolean genericIntersects(IIntervalsTimeDomain a, IIntervalsTimeDomain b) {
        // if boundary do not cross, they sure not intersect
        Interval aBounding = a.getBoundingInterval();
        Interval bBounding = b.getBoundingInterval();
        if (aBounding == null || bBounding == null) {
            return true;
        }
        
        if (!aBounding.intersects(bBounding)) {
            return false;
        }
        
        // if bounding interval intersects, test precisly
        IntervalsSet aIntervals = a.getIntervalsIn(aBounding);
        aIntervals.intersectWith(b.getIntervalsIn(bBounding));
        return !aIntervals.empty();
    }
    
    /**
     * Compute transitive closure of "intersects" relation on domains, that contains given seed.
     * Intersect relation is computed against given inRange interval. It is implemented
     * iterative where in each iteration new intersecting relations is searched.
     * It is in worst case O(n^3), like Warshall's algorithm
     * @param domains
     * @param seed
     * @return 
     */
    public static List<Integer> computeIntersectsTransitiveClosure(List<IIntervalsTimeDomain> domains, List<Integer> seed) {
        
        // set cache matrix for storing intersects relation
        IntMatrix intersects = new IntMatrix(domains.size());
        
        List<Integer> closure = new ArrayList(seed);
        List<Integer> added = new ArrayList<>(seed);
        
        while(added.isEmpty() == false) {
            // get last added
            int index = added.remove(added.size() - 1).intValue();
            
            // search for intersecting domains
            for (int i = 0; i < domains.size(); i++) {
                if (intersects.get(index, i) == 0) {
                    // intersection is not cached, compute it
                    boolean intersect = domains.get(index).intersects(domains.get(i));
                    intersects.set(index, i, intersect ? 1 : -1);
                    intersects.set( i, index, intersect ? 1 : -1);
                }
                
                // if intersects, add to closure, if not there
                if (intersects.get(index, i) > 0) {
                    if (!closure.contains(i)) {
                        closure.add(i);
                        added.add(i);
                    }
                }
            }
        }
        
        return closure;
    } 
    
}
