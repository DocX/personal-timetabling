/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.timedomain;

import net.personaltt.utils.Converter;
import org.joda.time.LocalDateTime;

/**
 * Converter of timedomain internals time representation in LocalDateTime to
 * numeric representation for solver. Converts time to number of seconds
 * since Unix epoch.
 * This implementation is inversible by ConverterFromInteger
 * @author docx
 */
public class ConverterToInteger implements Converter<LocalDateTime, Integer> {

    @Override
    public Integer convert(LocalDateTime source) {
        return (int)(source.toDateTime().getMillis()/1000l);
    }
}