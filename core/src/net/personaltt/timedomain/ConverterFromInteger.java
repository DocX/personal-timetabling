/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import net.personaltt.utils.Converter;
import org.joda.time.LocalDateTime;

/**
 * Converted from integer to internal representation of time in timedomain package
 * classes. It is inversible with ConverterToInteger.
 * @author docx
 */
public class ConverterFromInteger implements Converter<Integer, LocalDateTime> {

    @Override
    public LocalDateTime convert(Integer source) {
        return new LocalDateTime((long)source * 1000l);
    }
}