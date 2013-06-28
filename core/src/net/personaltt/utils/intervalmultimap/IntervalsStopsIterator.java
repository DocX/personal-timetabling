/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils.intervalmultimap;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Intervals stops iterator. Iterator of intervals stops on the
 * number line in some super interval. Implementation has to define upperBound
 * of that super interval on the number line. Then the last stop in interator 
 * is the last stop strictly before upperBound and. Implementation should always
 * iterate at least one stop (ie super interval lower bound).
 * @author docx
 */
public interface IntervalsStopsIterator<K,V> extends Iterator<Entry<K,V>> {
    
    /**
     * Upper bound of iterated intervals. Iterator should ends with last stop before
     * intervals end. This method returns stop, until which the last interval lies.
     * @return 
     */
    K upperBound();
    
}
