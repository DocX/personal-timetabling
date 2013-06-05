package net.personaltt.simplesolver;

import java.util.List;
import java.util.Random;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.ProblemDefinition;
import net.personaltt.problem.Schedule;

/**
 * Simple solver finds any feasible schedule for personal timetabling problem.
 * @author docx
 */
public class SimpleSolver {

    private Schedule workingSchedule;
    
    long timeoutLimit = 15000;
    
    public Schedule solve(ProblemDefinition problem) {
        workingSchedule = (Schedule)problem.initialSchedule.clone();
        
        // Prepare conflicting occurrences
        OccurrencesItervalsMultiset currentAllocationIntevals = new OccurrencesItervalsMultiset(workingSchedule);
        
        List<Occurrence> conflictingOccurrences = currentAllocationIntevals.getConflictingOccurrences();
        
        // Prepare utilities
        long startTime = System.currentTimeMillis();
        Random random = new Random();
        
        // while there is conflict, solve it
        while(conflictingOccurrences.size() > 0 && System.currentTimeMillis() - startTime < timeoutLimit) {
            
            int toSolveIndex = random.nextInt(conflictingOccurrences.size());
            Occurrence toSolve = conflictingOccurrences.get(toSolveIndex);
            
            // solve conflict. 
            // get domain of occurrence, subtract current schedule without this
            // occurrence from it 
            // and find nearest interval with smallest number of occurrences allocated on it
            
            
        }
        
        return null;
        
    }
    
}
