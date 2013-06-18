/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.main;

import java.util.Map;
import java.util.Random;
import net.personaltt.problem.Occurrence;
import net.personaltt.problem.OccurrenceAllocation;
import net.personaltt.problem.ProblemDefinition;
import net.personaltt.problem.Schedule;
import net.personaltt.simplesolver.SimpleSolver;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;

/**
 *
 * @author docx
 */
public class Benchmark {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // solve
        SimpleSolver solver = new SimpleSolver();
        solver.timeoutLimit = 60000;
        
        //ProblemDefinition problem = SeqentialProblem(1000);
        ProblemDefinition problem = SeqentialProblemInitialyFilled(1000);
        Schedule solved = solver.solve(problem); 
        
        // print solution
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : solved.getOccurrencesAllocations()) {
            System.out.printf("%s: %s\n", entry.getKey().getId(), entry.getValue().toString());
            
        }
        System.out.printf("Conflicting: %s", solved.hasConflict());
        
    }
    
    // simple problem
    // all occurrences will have same domain
    // length of domain is sum of minimal lengths of all occurrences
    public static ProblemDefinition SeqentialProblem(int number) {
        BaseIntervalsSet<Integer> domain1 = new BaseIntervalsSet<>();
        domain1.unionWith(0,10*number);
                
        // problem occurrences with all conflicting
        ProblemDefinition problem = new ProblemDefinition();
        
        for (int i = 0; i < number; i++) {
            problem.addOccurrence(new Occurrence(domain1, 10, 100, i), new BaseInterval<>(0, 10));
        }
        
        return problem;        
    }
    
    
    // simple problem
    // all occurrences will have same domain
    // length of domain is sum of minimal lengths of all occurrences
    public static ProblemDefinition SeqentialProblemInitialyFilled(int number) {
        BaseIntervalsSet<Integer> domain1 = new BaseIntervalsSet<>();
        domain1.unionWith(0,10*number);
                
        // problem occurrences with all conflicting
        ProblemDefinition problem = new ProblemDefinition();
        
        for (int i = 0; i < number; i++) {
            problem.addOccurrence(new Occurrence(domain1, 10, 10*number, i), new BaseInterval<>(0, 10*number));
        }
        
        return problem;        
    }
}
