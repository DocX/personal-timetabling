/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import net.personaltt.utils.Metric;

/**
 * Integer metric.
 * @author docx
 */
public class IntegerMetric implements Metric<Integer, Integer> {

    @Override
    public Integer add(Integer to, Integer amount) {
        return to + amount;
    }

    @Override
    public Integer removes(Integer from, Integer amount) {
        return from - amount;
    }

    @Override
    public Integer difference(Integer a, Integer b) {
        return a - b;
    }
    
}
