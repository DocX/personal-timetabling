/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.personaltt.client;

import net.personaltt.model.ProblemDefinition;
import net.personaltt.model.Schedule;
import net.personaltt.solver.core.Solver;
import net.personaltt.solver.core.SolverSolution;

/**
 * Solver client. Encapsulate solver running in separate thread.
 * @author docx
 */
public class SolverClient {

    SolverThread thread;
    
    public SolverClient() {
        
    }
    
    public void solve(ProblemDefinition problem, long timeout) throws Exception {
        if (thread != null) {
            throw new Exception("Cannot start twice with same instance");
        }
        
        thread = new SolverThread(problem);
        thread.solver.timeoutLimit = timeout;
        
        System.out.println("Solver started");
        thread.start();
    }
    
    public Schedule getCurrentBest() throws Exception {
        if (thread == null) {
            throw new Exception("Not started yet");
        }
         
        return thread.getBestSolution().getSchedule();
    }
    
    public boolean isRunning() throws Exception {
        if (thread == null) {
            throw new Exception("Not started yet");
        }
        
        return thread.isAlive();
    }
    
    public boolean endedWithError()  throws Exception {
        if (thread == null) {
            throw new Exception("Not started yet");
        }
        
        return thread.ex != null;
    }
    
    public void stop() throws Exception {
        if (thread == null) {
            throw new Exception("Not started yet");
        }
        
        thread.sendStop();
    }
    
    public void join() throws Exception {
        thread.join();
    }

    @Override
    protected void finalize() throws Throwable {
        if (thread != null && thread.isAlive()) {
            thread.sendStop();
        }
        super.finalize();
    }
    
    private class SolverThread extends Thread {
        
        Solver solver;
        ProblemDefinition problem;
        Exception ex;

        public SolverThread(ProblemDefinition problem) {
            solver = new Solver();
            this.problem = problem;
        }

        @Override
        public void run() {
            try {
                solver.solve(problem);
            } catch (Exception e) {
                ex = e;
            }
        }
        
        public void sendStop() {
            solver.timeoutLimit = 0;
            solver.step();
        }
        
        public SolverSolution getBestSolution() {
            return solver.currentBestSolution();
        }
    }
}
