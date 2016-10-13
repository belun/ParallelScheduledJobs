package repede.alexandru.poc.test;

import org.apache.commons.lang3.time.StopWatch;
import repede.alexandru.poc.RunnableJob;


public class TimeBombJob extends RunnableJob {

    private final int delay;
    private final Runnable runFinally;

    public TimeBombJob(int delay, Runnable runFinally) {
        this.delay = delay;
        this.runFinally = runFinally;
    }

    @Override
    public void execute() {
        try {
            System.out.format("\nTimeBomb: Running for %d", delay);
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();
            Thread.sleep(delay);
            stopwatch.stop();
            System.out.format("\nTimeConsuming: Exploding after %d", stopwatch.getTime());
            throw new RuntimeException("TimeBomb: Simulated time-triggered error");
        } catch (InterruptedException e) {
            throw new RuntimeException("TimeBomb: I have been interrupted");
        } finally {
            runFinally.run();
        }
    }
}
