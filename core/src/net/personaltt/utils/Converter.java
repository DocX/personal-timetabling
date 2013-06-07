/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.utils;

/**
 * Converter interfaces provides method for converting objects from one type to 
 * another or any casting of object.
 * @author docx
 */
public interface Converter<S,D> {
    public D convert(S source);
}
