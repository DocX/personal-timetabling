/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import net.personaltt.client.ProblemDefinitionBuilder;
import net.personaltt.client.SolverClient;
import net.personaltt.model.Occurrence;
import net.personaltt.model.OccurrenceAllocation;
import net.personaltt.model.ProblemDefinition;
import net.personaltt.model.Schedule;
import net.personaltt.solver.core.Solver;
import net.personaltt.solver.core.SolverSolution;
import net.personaltt.solver.core.SolverState;
import net.personaltt.solver.heuristics.MainSolverState;
import net.personaltt.timedomain.RepeatingIntervalDomain;
import net.personaltt.utils.BaseInterval;
import net.personaltt.utils.BaseIntervalsSet;
import net.sf.cpsolver.ifs.util.DataProperties;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;

/**
 *
 * @author docx
 */
public class Benchmark {

    static int SOLVER_TIMEOUT = 1000*60*30;
    
    static DataProperties solverProperties = new DataProperties();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //ProblemDefinition problem;
        
        // select problem
        //problem =
        //bench1(100);
        //bench2();
        //bench3();
        //bench4(100);
        //bench5(2);
        //testSolverClient();
        //benchPriority(1);
        
        // set some values if want
        //solverProperties.setProperty("mainAllocationSelection.stayMinConflictSelectionAfterToZeroConflict", "2");
        //solverProperties.setProperty("mainAllocationSelection.probBestIgnoringSelWhenNoConflict", "1");
        
        
        // RUN ONE OF FOLLOWING
        
        //// start solving selected problem
        //SolverState solution = benchSolve(problem);
        //printBestSolution(solution);
        
        //// benchmark number of iteration in what must not fall conflict area
        //// to determine minimal conflict area and start solving preference
        //benchmarkProp(problem, "mainAllocationSelection.stayMinConflictSelectionAfterToZeroConflict", 5, 25, 5);
        
        //// benchmark probBestIgnoringSelWhenNoConflict
        //benchmarkProp(problem, "mainAllocationSelection.probBestIgnoringSelWhenNoConflict", 0, 1, 0.2);
        
        //// benchmark optimizationWhenConflictProb
        //benchmarkProp(problem, "rouletteOccurrenceSelection.optimizationWhenConflictProb", 0, 1, 0.2);
       
        //// benchmark priority
        //benchmarkPriority();
        
        //benchmarkTerminationCondition();
        
        testAll();
    }

    public static void testAll() {
        test1(100);
        test2(100);
        test3(100);
        test4(100);
        testS(1);
        testP(50);
        
        test1(500);
        test1(1000);
        test1(5000);     
        
        test2(500);
        test2(1000);
        test2(5000);
        
        
        test3(500);
        test3(1000);
        test3(5000);
        
        
        test4(500);
        test4(1000);
        test4(5000);
        
        
        testS(10);
        testS(100);
        testS(500);
        
        
        testP(100);
    }
    
    public static void test1(int number) {
        System.out.printf("PROBLEM 1 - %s events\n", number);
        ProblemDefinition p = bench1(number);
        printInitialCosts(p);
        testProblem(p);
    }
    
    public static void test2(int number) {
        System.out.printf("PROBLEM 2 - %s events\n", number);
        ProblemDefinition p = bench2(number);
        printInitialCosts(p);
        testProblem(p);
    }
    
    public static void test3(int number) {
        System.out.printf("PROBLEM 3 - %s events\n", number);
        ProblemDefinition p = bench3(number);
        printInitialCosts(p);
        testProblem(p);
    }
    
    public static void test4(int number) {
        System.out.printf("PROBLEM 4 - %s events\n", number);
        ProblemDefinition p = bench4(number);
        printInitialCosts(p);
        testProblem(p);
    }
    
    public static void testS(int number) {
        System.out.printf("PROBLEM S - %s number\n", number);
        ProblemDefinition p = bench5(number);
        printInitialCosts(p);
        testProblem(p);
    }
    
    public static void testP(int number) {
        System.out.printf("PROBLEM P - %s number\n", number);
        ProblemDefinition p = benchPriority(1,number);
        printInitialCosts(p);
        testProblem(p);
    }    

    private static void printInitialCosts(ProblemDefinition p) {
           
        MainSolverState currentSolution = new MainSolverState();
        currentSolution.init(p.initialSchedule);
        
        System.out.println(String.format("INITIAL COST: c:%s p:%s", currentSolution.constraintsCost(), currentSolution.preferenceCost()));
    }
    
 
    
    
    public static void benchmarkPriority() {
        List<String> results = new ArrayList<String>();
        for (int i = 0; i <= 0; i+=1) {
            int nonPrefCount = 0;
            for (int j = 0; j < 10; j++) {
                ProblemDefinition problem = benchPriority(i,50);
                SolverState state = benchSolve(problem);
                if (state.getBestSolution().getSchedule().getAllocationOf(1).getStart() != 0) {
                    nonPrefCount++;
                }
            }
            results.add(String.format("Priority: %s, not preffered count: %s/10", i, nonPrefCount));
        }
       
        for (String string : results) {
            System.out.println(string);
        }
    }
    
    /**
     * Experimental benchmark for determination relation between
     * number of events and interation in whish is found best solution
     */
    public static void benchmarkTerminationCondition() {
        List<String> results = new ArrayList<String>();
        for (int j = 100; j <= 1000; j+=100) {
            ProblemDefinition problem = bench1(j);
            SolverState state = benchSolve(problem);
            results.add(String.format("Events: %s, best iteration: %s, end iteration: %s",
                    j, 
                    state.getLastBestIteration(),
                    state.getItearation()
                    ));
        }
       
        for (String string : results) {
            System.out.println(string);
        }
    }
    
    public static void benchmarkProp(ProblemDefinition problem, String prop, double min, double max, double step) {
        
        List<String> results = new ArrayList<>();
        
        for (double stay = min; stay <= max; stay+= step) {
            long ccost = 0;
            long pcost = 0;
            long besti = 0;
            for (int i = 0; i < 5; i++) {
                solverProperties.setProperty(prop, String.valueOf(stay));
                
                SolverState state = benchSolve(problem);
                results.add(
                        String.format("test prop:%s, i:%s result: best c:%s p:%s it:%s",
                        stay,
                        i, 
                        state.getBestSolution().constraintsCost(), 
                        state.getBestSolution().preferenceCost(), 
                        state.getLastBestIteration())
                       );
                
                ccost += state.getBestSolution().constraintsCost();
                pcost += state.getBestSolution().preferenceCost();
                besti += state.getLastBestIteration();
            }
            results.add(String.format("prop %s avg: best c:%s p:%s it:%s",
                    stay,
                    ccost / 5f,
                    pcost / 5f,
                    besti / 5f
                    ));
        }
        
        System.out.println();
        
        for (String string : results) {
            System.out.println(string);    
        }
        
    }
    
    
    
    /**
     * Simple problem bench. All occurrences have shared one domain
     * that is long exactly as sum of minimal durations. Initialy all
     * occurrences are placed on the start of domain with minimal duration.
     */
    private static ProblemDefinition bench1(int number) {
        ProblemDefinition problem = SeqentialProblem(number);
        //printProblem(problem.problemOccurrences);
        return problem;
    }
    
    /**
     * Shared domain filled. All occurrences have one shared domain that
     * is long as exactly the sum of minimal durations - all occurrences fits
     * only when all are minimal duration long.
     * Initialy all occurrences fills entire domain.
     */
    private static ProblemDefinition bench2(int number) {
        ProblemDefinition problem = SeqentialProblemInitialyFilled(number);
        //printProblem(problem.problemOccurrences);
        return (problem);
    }
    
    /**
     * Random problem. Random problem with given number of occurrences in given
     * time horizont. It may be overconstrained - no not-conflicting solution exists.
     */
    private static ProblemDefinition bench3(int number) {
        ProblemDefinition problem = (new RandomProblemGenerator(24*60*60*30, number, 0.2f)).generateRandomProblem();
        //printProblem(problem.problemOccurrences);
        
        return (problem);
    }
    
    /**
     * Random non conflicting problem. Random problem which is build from
     * valid solution. That solution is also the best one in terms of max duration.
     * So this bench can be compared to determined optimum. 
     */
    private static ProblemDefinition bench4(int number) {
        SolvableProblemGenerator generator = new SolvableProblemGenerator(24*60*60*30, number);
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
        System.out.println();
        System.out.println("Optimal solution:");
        //printProblem(generator.optimalSolution);
        
        // print initial problem
        System.out.println("Initial problem:");
        //printProblem(problem.problemOccurrences);
        
        // Print optimal cost sum
        long optimalCostSumInInitial = 0;
        for (int i = 0; i < problem.problemOccurrences.size(); i++) {
            optimalCostSumInInitial += problem.problemOccurrences.get(i)
                    .getPreferrenceCost(generator.optimalSolution.get(i).getAllocation());
        }
        System.out.printf("Optimal solution cost: %s\n", optimalCostSumInInitial);
        
        System.out.println();
        return (problem);
    }
    
    /**
     * Stuck test bench. Problem where to obtain lower cost from
     * initial must be placed conflicting allocation.
     */
    private static ProblemDefinition bench5(int number) {
        ProblemDefinition problem = new ProblemDefinition();
        
        for (int i = 0; i < number; i++) {
             
            int start = 40*i;
            
            BaseIntervalsSet<Integer> domain1 = new BaseIntervalsSet<>();
            domain1.unionWith(start,start+20);

            BaseIntervalsSet<Integer> domain2 = new BaseIntervalsSet<>();
            domain2.unionWith(start,start+20);
            domain2.unionWith(start+30,start+40);

            // initial complicated problem with no conflict but not maximal duration sum
            problem.addOccurrence(new Occurrence(domain1, 10, 20, 3*i + 1), new BaseInterval<>(start,start+10));
            problem.addOccurrence(new Occurrence(domain2, 10, 20, 3*i +2), new BaseInterval<>(start+10,start+20));
            problem.addOccurrence(new Occurrence(domain2, 5, 5, 3*i + 3), new BaseInterval<>(start+30,start+35));
        }
       
        // problem i
        // 0     10    20    30    40
        // |-----|
        //       |-----|
        //                   |--|
        
        // optimal solution in duration sum is
        // 1: 0-15
        // 2: 30-40
        // 3: 15-20
        
        // print initial problem
        //System.out.println("\nInitial problem:");
        //printProblem(problem.problemOccurrences);
        
        System.out.println();
        return (problem);
    }
    
    private static ProblemDefinition benchPriority(int priority, int number) {
        ProblemDefinition problem = new ProblemDefinition();
        
        BaseIntervalsSet<Integer> domain = new BaseIntervalsSet<>();
        domain.unionWith(0,number*10);
        
        Occurrence priorityOccurrence = new Occurrence(domain, 10,10, 1);
        problem.addOccurrence(priorityOccurrence, new BaseInterval<>(0,10));     
        priorityOccurrence.setPreferrencePriority(priority);
        
        for (int i = 2; i <= number; i++) {
            problem.addOccurrence(new Occurrence(domain, 10,10, i), new BaseInterval<>(0,10));            
        }
        
        return problem;
    }
    
    
    private static void testSolverClient() {
        ProblemDefinition problem = SeqentialProblem(1000);
       
        SolverClient client = new SolverClient();
        try{
            client.solve(problem, SOLVER_TIMEOUT);

            System.out.printf("Running %s, error %s\n", client.isRunning(), client.endedWithError());
            client.join();
            System.out.printf("Running after join %s, error %s\n", client.isRunning(), client.endedWithError());
        
        } catch(Exception e) {
            System.out.println("Exception caught");
        }
        
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
    
    private static SolverState benchSolve(ProblemDefinition problem) {
       
        
        // solve
        Solver solver = new Solver();
        solver.timeoutLimit = SOLVER_TIMEOUT;
        solver.properties = solverProperties;
        
        SolverThread run = new SolverThread(solver, problem);
        //run.solver.pause();
        run.start();
//        if (System.console() != null) {
//            run.solver.pause();
//        while(run.isAlive()) {
//            String cmd = System.console().readLine();
//            System.out.println(cmd);
//            
//            switch(cmd) {
//                case "p":
//                    run.solver.pause();
//                    break;
//                case "s":
//                    System.out.println("Step");
//                    run.solver.step();
//                    break;
//                case "r":
//                    System.out.println("Printing state");
//                    run.solver.printState();
//                    break;
//                case "q":
//                    run.solver.timeoutLimit = 0;
//                    run.solver.step();
//                    break;
//            }
//        }
//        }
        try {
            run.join();
        } catch(Exception e) {
            
        }
        
        
        return run.state;
    }

    
    private static void printBestSolution(SolverState state) {
        SolverSolution best = state.getBestSolution();
        
        // print solution
        for (Map.Entry<Occurrence, OccurrenceAllocation> entry : best.getSchedule().getOccurrencesAllocations()) {
            entry.getKey().setAllocation(entry.getValue());
            System.out.printf("%s\ta:%s\tc:%s:%s:%s\n", 
                    entry.getKey().getId(), 
                    entry.getValue().toString(), //allocation
                    entry.getKey().getPreferrenceCost(),
                    entry.getKey().getDurationCost(),
                    entry.getKey().getPreferredStartCost()
                    );
            
        }
        System.out.printf("Conflicting: %s, Cost: %s, Found in: %s", 
                best.constraintsCost(), 
                best.preferenceCost(), 
                state.getLastBestIteration());
        
    }

    private static void testProblem(ProblemDefinition p) {
        long startTime = System.currentTimeMillis();
        SolverState state = benchSolve(p);
        System.out.println(String.format("END in %s, iterations %s", System.currentTimeMillis() - startTime, state.getItearation()));
        System.out.println("RESULTS");
        System.out.printf("ConflictBest:%s, PrefBest:%s, IterBest:%s\n", 
                state.getBestSolution().constraintsCost(), 
                state.getBestSolution().preferenceCost(), 
                state.getLastBestIteration()
        );
        System.out.println();
        System.out.println();
    }
     
    private static class SolverThread extends Thread {

        public boolean paused = false;
        
        Solver solver;
        ProblemDefinition problem;
        SolverState state;

        public SolverThread(Solver solver, ProblemDefinition problem) {
            this.solver = solver;
            this.problem = problem;
        }
        
        
        @Override
        public void run() {
            state = solver.solve(problem);
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
    
    /**
     * Definition of real world problem of Bob from the thesis. Office work, meetings, lunch, 
     * child pickup from school, 
     * @param weeks
     * @return 
     */
    private static ProblemDefinition BobProblem(int weeks) {
        ProblemDefinitionBuilder pb = new ProblemDefinitionBuilder();
        
        LocalDateTime refDate = new LocalDateTime(2013,7,1,0,0,0);
        
        RepeatingIntervalDomain lunchDomain = new RepeatingIntervalDomain(refDate.plusHours(11), Hours.THREE, Days.ONE);
        RepeatingIntervalDomain officeDomain = new RepeatingIntervalDomain(refDate.plusHours(9), Hours.SIX, Days.ONE);
        
        
        for (int i = 0; i < weeks; i++) {
            int eventsInWeek = 1;
            LocalDateTime weekStart = refDate.plusWeeks(i);
            
            // for all week days
            for (int j = 0; j < 5; j++) {
                pb.addOccurrence(eventsInWeek*i + 0,weekStart.plusHours(j*24 + 11), 60, 30,60, lunchDomain);
                pb.addOccurrence(eventsInWeek*i + 1,weekStart.plusHours(j*24 + 9), 3*60, 30,60, officeDomain);
            }
            
            
                    
           
        }
        
        return pb.getDefinition();
    }
    
}
