/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.model.ProblemDefinition;
import net.personaltt.model.Schedule;
import net.personaltt.solver.core.Solver;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;

/**
 *
 * @author docx
 */
public class Benchmark {

    static int SOLVER_TIMEOUT = 90000;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        bench1();
        //bench2();
        //bench3();
        //bench4();
        //bench5();
    }
    
    /**
     * Simple problem bench. All occurrences have shared one domain
     * that is long exactly as sum of minimal durations. Initialy all
     * occurrences are placed on the start of domain with minimal duration.
     */
    private static void bench1() {
        ProblemDefinition problem = SeqentialProblem(1000);
        printProblem(problem.problemOccurrences);
        benchSolve(problem);
    }
    
    /**
     * Shared domain filled. All occurrences have one shared domain that
     * is long as exactly the sum of minimal durations - all occurrences fits
     * only when all are minimal duration long.
     * Initialy all occurrences fills entire domain.
     */
    private static void bench2() {
        ProblemDefinition problem = SeqentialProblemInitialyFilled(1000);
        //printProblem(problem.problemOccurrences);
        benchSolve(problem);
    }
    
    /**
     * Random problem. Random problem with given number of occurrences in given
     * time horizont. It may be overconstrained - no not-conflicting solution exists.
     */
    private static void bench3() {
        ProblemDefinition problem = (new RandomProblemGenerator(24*60*60, 100, 0f)).generateRandomProblem();
        printProblem(problem.problemOccurrences);
        benchSolve(problem);
    }
    
    /**
     * Random non conflicting problem. Random problem which is build from
     * valid solution. That solution is also the best one in terms of max duration.
     * So this bench can be compared to determined optimum. 
     */
    private static void bench4() {
        SolvableProblemGenerator generator = new SolvableProblemGenerator(24*60*60, 1000);
        /*
         * seed 22108929:
         * stucks on costs of 
         * * 7, 24, ?
         * * occurrence id 345 has tendency to stuck on 7
         * * occurrence id 643 has tendency fo stuck on 17
         * 
         */
        generator.r = new Random(22108929);
        
        
        generator.generateRandomProblem();
        ProblemDefinition problem = generator.getProblem();
        
        // print optimal solutions
        System.out.println("\nOptimal solution:");
        printProblem(generator.optimalSolution);
        
        // print initial problem
        System.out.println("\nInitial problem:");
        printProblem(problem.problemOccurrences);
        
        System.out.println();
        benchSolve(problem);
    }
    
    /**
     * Stuck test bench. Problem where to obtain lower cost from
     * initial must be placed conflicting allocation.
     */
    private static void bench5() {
        ProblemDefinition problem = new ProblemDefinition();
        
        BaseIntervalsSet<Integer> domain1 = new BaseIntervalsSet<>();
        domain1.unionWith(0,20);
        
        BaseIntervalsSet<Integer> domain2 = new BaseIntervalsSet<>();
        domain2.unionWith(0,20);
        domain2.unionWith(30,40);
        
        // initial complicated problem with no conflict but not maximal duration sum
        problem.addOccurrence(new Occurrence(domain1, 10, 20, 1), new BaseInterval<>(0,10));
        problem.addOccurrence(new Occurrence(domain2, 10, 20, 2), new BaseInterval<>(10,20));
        problem.addOccurrence(new Occurrence(domain2, 5, 5, 3), new BaseInterval<>(30,35));
        
        // optimal solution in duration sum is
        // 1: 0-15
        // 2: 30-40
        // 3: 15-20
        
        // print initial problem
        System.out.println("\nInitial problem:");
        printProblem(problem.problemOccurrences);
        
        System.out.println();
        benchSolve(problem);
    }
    
    
    private static void printProblem(Iterable<Occurrence> problem) {
          // print optimal problem solutio
        for (Occurrence entry : problem) {
            System.out.printf("%s\tdl:%s\tdu:%s\ti:%s\tT:%s\n", 
                    entry.getId(),
                    entry.getMinDuration(),
                    entry.getMaxDuration(),
                    entry.getAllocation().toString(),
                    entry.getDomain() == null ? "NA" : entry.getDomain().getBaseIntervals().toString()
                    );
            
        }
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

    private static void benchSolve(ProblemDefinition problem) {
       
        
        // solve
        Solver solver = new Solver("net.personaltt.solver.heuristics.RouletteOccurrenceSelection", "net.personaltt.solver.heuristics.MainAllocationSelection");
        solver.timeoutLimit = SOLVER_TIMEOUT;
        Schedule solved = solver.solve(problem); 
        
        // print solution
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : solved.getOccurrencesAllocations()) {
            System.out.printf("%s\ta:%s\tc:%s\n", 
                    entry.getKey().getId(), 
                    entry.getValue().toString(), //allocation
                    entry.getKey().getAllocationCost()
                    );
            
        }
        System.out.printf("Conflicting: %s", solved.hasConflict());
    }
    
    
    private static class RandomProblemGenerator {
        ProblemDefinition problem;
        Random r = new Random();
        int maxTime;
        int number; 
        float fixedChance;

        public RandomProblemGenerator(int maxTime, int number, float fixedChance) {
            this.maxTime = maxTime;
            this.number = number;
            this.fixedChance = fixedChance;
        }
        
       
        public ProblemDefinition generateRandomProblem() {
            problem = new ProblemDefinition();

            for (int i = 0; i < number; i++) {
                boolean fixed = Math.random() <= fixedChance;
                
                if (fixed) {
                    addRandomFixed(i);
                } else {
                    addRandomFloating(i);
                } 
                    
            }
            
            return problem;

        }
        
        private void addRandomFixed(int id) {
               
            // 5 minutes to 8 hours in seconds
            int duration = r.nextInt(8*60*60 - 300) + 300;
            
            // domain and initial allocation
            BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
            BaseInterval<Integer> initial;
            
            // only one fixed domain
            int start = r.nextInt(maxTime - duration);
            domain.unionWith(start, start+duration);
            
            problem.addOccurrence(new Occurrence(domain, duration, duration, id), new BaseInterval<>(start, start+duration ) );
        }
        
        private void addRandomFloating(int id) {
             BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
             BaseInterval<Integer> initial;
                
            // 5 minutes to 8 hours in seconds
            int minDuration = r.nextInt(8*60*60 - 300) + 300;
            // fixed or up to 8 hours
            int maxDuration = r.nextInt(8*60*60 - minDuration) + minDuration;
             
            // get number of domain chunks
            int domainIntervals = r.nextInt(5) + 1;

            int leastStart = Integer.MAX_VALUE;
            for (int j = 0; j < domainIntervals; j++) {

                // somewhere in the scope, but ensure at least minDuration space
                int start = r.nextInt(maxTime - minDuration);
                int end = r.nextInt(maxTime-start-minDuration)+start+minDuration;

                domain.unionWith(start, end);
                
                if (start < leastStart) {
                    leastStart = start;
                }
            }
            
            // select initial. by default the least position
            initial = new BaseInterval<>(leastStart, leastStart+minDuration);
            
            problem.addOccurrence(new Occurrence(domain, minDuration, maxDuration, id), initial);
        }
    }
    
    /**
     * generates valid schedule of events, build domains around it and then 
     * randomly shufle it
     */
    private static class SolvableProblemGenerator {
        
        ProblemDefinition problem;
        Random r = new Random();
        int maxTime;
        int number; 
        //float fixedChance;
        long bestSolutionDurations = 0;
        List<Occurrence> optimalSolution;
        
        public SolvableProblemGenerator(int maxTime, int number/*, float fixedChance*/) {
            this.maxTime = maxTime;
            this.number = number;
            //this.fixedChance = fixedChance;
        }

        public ProblemDefinition getProblem() {
            return problem;
        }
        
        
        
        public void generateRandomProblem() {
            problem = new ProblemDefinition();
            bestSolutionDurations = 0;
            optimalSolution = new ArrayList<>(number);

            // first genearete random distinct and sorted list of
            // stops. odd stop will be starts of events, even stops will be ends
            TreeSet<Integer> stops = new TreeSet<>();
           
            for (int i = 0; i < number*2; i++) {
               
                // get random number from whole space, without i numbers
                // which are already used
                int stop = r.nextInt(maxTime-i);
                
                // walk by sorted list of stops and for each value that 
                // random stop is greater or equal add one to stop
                for (Integer integer : stops) {
                    if (integer <= stop) {
                        stop += 1;
                    } else {
                        break;
                    }
                }
                
                // add stop
                stops.add(stop);
            }
            
            // now build occurrences and domains around it
            int id = 0;
            for (Iterator<Integer> it = stops.iterator(); it.hasNext();) {
                int start = it.next().intValue();
                int end = it.next().intValue();
                
                // use interval as maxDuration (optimal solution)
                // select minDuration to be at least half of maxDuration
                int maxDuration = (end-start);
                int minDuration = maxDuration - r.nextInt(maxDuration / 2 + 1);
                bestSolutionDurations += maxDuration;
                
                addToOptimal(id, start, end, minDuration, maxDuration);
                
                BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
                
                // create domain that contains this star and end
                domain.unionWith(r.nextInt(start+1), end+r.nextInt(maxTime-end));
                
                // and random number of auxiliary domains intervals, larger than minDuration
                int auxDomains = 50;
                for (int i = 0; i < auxDomains; i++) {
                    int i_begin = r.nextInt(maxTime - minDuration);
                    int i_end = r.nextInt(maxTime - i_begin-minDuration)+i_begin+minDuration;
                    domain.unionWith(i_begin, i_end);
                }
                
                // now shuffle allocation somewhere whithin domain
                List<BaseInterval<Integer>> domainIntervals = domain.getBaseIntervals();
                BaseInterval<Integer> randomInterval = domainIntervals.get(r.nextInt(domainIntervals.size()));
                
                int init_start = randomInterval.getStart() + r.nextInt(randomInterval.getEnd() - randomInterval.getStart() - minDuration +1);
                // end must be at least start + minDuration, at most start + maxDuration
                // must be before randominterval.getEnd
                int init_end = init_start + minDuration + 
                        // what is left to the end of interval, or maxDuration respectively
                        r.nextInt(Math.min(randomInterval.getEnd() - init_start, maxDuration-minDuration) +1);
                
                // and add it together to problem
                problem.addOccurrence(new Occurrence(domain, minDuration, maxDuration, id), new BaseInterval<>(init_start, init_end));
                id++;
            }
            
            

        }

        private void addToOptimal(int id, int start, int end, int minDuration, int maxDuration) {
            Occurrence occurrence = new Occurrence(null, minDuration, maxDuration, id);
            occurrence.setAllocation(new OccurrenceAllocation(start, end - start));
            optimalSolution.add(occurrence);
        }
    }
    
}
