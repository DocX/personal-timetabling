/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

 /**
  * Values interval. Interval and its value
  */
public class ValuedInterval<K extends Comparable, V> extends BaseInterval<K> {
    V values;

    public V getValues() {
        return values;
    }


    public ValuedInterval(BaseInterval<K> interval, V values) {
        super(interval.start, interval.end);
        this.values = values;
    }
}
