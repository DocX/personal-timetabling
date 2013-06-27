/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

import java.util.Random;

/**
 *
 * @author docx
 */
public class RandomUtils {
    
    /**
     * Generate random long number greater than or equal to 0 and lesser than given n
     * @param rng Random instance
     * @param n Exclusive upper bound of number generated
     * @return 
     */
    public static long nextLong(Random rng, long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }

        long bits, val;
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }
}
