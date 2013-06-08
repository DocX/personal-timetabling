/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.client;

import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.Schedule;
import net.personaltt.timedomain.ConverterFromInteger;
import net.personaltt.utils.Converter;
import org.joda.time.LocalDateTime;

/**
 * Schedule parser converts problem schedule representation to
 * representation of LocalDateTime starts and seconds duration
 * @author docx
 */
public class ScheduleParser {
    
    Schedule schedule;
    
    Converter<Integer, LocalDateTime> converter;

    public ScheduleParser(Schedule schedule) {
        this.schedule = schedule;
        converter = new ConverterFromInteger();
    }
    
    /**
     * Allocation values for parser user
     */
    public class Allocation {
        public LocalDateTime start;
        public int duration;

        public Allocation(LocalDateTime start, int duration) {
            this.start = start;
            this.duration = duration;
        }
    }
    
    public Allocation getAllocationOf(int id) {
        OccurrenceAllocation allocation = schedule.getAllocationOf(id);
        if (allocation == null) {
            return null;
        } else {
            return new Allocation(
                    converter.convert(allocation.getStart()),
                    allocation.getDuration()
                    );
        }
    }
    
}
