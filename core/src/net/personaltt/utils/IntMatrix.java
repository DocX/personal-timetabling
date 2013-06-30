/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

/**
 * Matrix of boolean values. Implemented as single array
 * @author docx
 */
 public class IntMatrix {
        int[] values;
        int order;

        public IntMatrix(int matrixOrder) {
            order = matrixOrder;
            values = new int[matrixOrder*matrixOrder];
        }      
        
        public void set(int i, int j, int value) {
            values[i * order + j] = value;
        }
        
        public int get(int i, int j) {
            return values[i*order + j];
        }
    }
