/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.model.Schedule;
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
        public int id;

        public Allocation(LocalDateTime start, int duration, int id) {
            this.start = start;
            this.duration = duration;
            this.id = id;
        }
    }
    
    public Allocation getAllocationOf(int id) {
        OccurrenceAllocation allocation = schedule.getAllocationOf(id);
        if (allocation == null) {
            return null;
        } else {
            return new Allocation(
                    converter.convert(allocation.getStart()),
                    allocation.getDuration(),
                    id
                    );
        }
    }
    
    /**
     * Returns list of schedule allocations with occurrences ids
     * @return 
     */
    public List<Allocation> getAllocations() {
        List<Allocation> allocations  = new ArrayList<>();
        
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : schedule.getOccurrencesAllocations()) {
            allocations.add(new Allocation(
                    converter.convert(entry.getValue().getStart()),
                    entry.getValue().getDuration(),
                    entry.getKey().getId()
                    ));
        }
        
        return allocations;
    }
    
}
