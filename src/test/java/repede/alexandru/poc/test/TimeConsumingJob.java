package repede.alexandru.poc.test;

import org.apache.commons.lang3.time.StopWatch;
import repede.alexandru.poc.RunnableJob;


public class TimeConsumingJob extends RunnableJob {

    private final int delay;
    private final Runnable successful;
    private final Runnable interruption;

    public TimeConsumingJob(int delay, Runnable successful, Runnable interruption) {
        this.delay = delay;
        this.successful = successful;
        this.interruption = interruption;
    }

    public TimeConsumingJob(int delay, Runnable successful) {
        this(delay, successful, () -> {} );
    }

    @Override
    public void execute() {
        try {
            System.out.format("\nTimeConsuming: Running for %d", delay);
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();
            Thread.sleep(delay);
            stopwatch.stop();
            System.out.format("\nTimeConsuming : Run for %d", stopwatch.getTime());
            successful.run();
        } catch (InterruptedException e) {
            interruption.run();
            System.out.format("\nTimeConsuming: I have been interrupted");
            throw new RuntimeException("I have been interrupted");
        }
    }
}
