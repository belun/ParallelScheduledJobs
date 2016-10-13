package repede.alexandru.poc.test;

import org.apache.commons.lang3.time.StopWatch;
import repede.alexandru.poc.RunnableJob;


public class TimeConsumingJob extends RunnableJob {

    private final int delay;
    private final Runnable after;

    public TimeConsumingJob(int delay, Runnable after) {
        this.delay = delay;
        this.after = after;
    }

    @Override
    public void execute() {
        try {
            System.out.format("\nRunning for : %d", delay);
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();
            Thread.sleep(delay);
            stopwatch.stop();
            System.out.format("\nRun for : %d", stopwatch.getTime());
            after.run();
        } catch (InterruptedException e) {
            throw new RuntimeException("I have been interrupted");
        }
    }
}