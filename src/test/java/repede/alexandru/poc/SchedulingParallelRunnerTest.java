package repede.alexandru.poc;

import net.jodah.concurrentunit.Waiter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import repede.alexandru.poc.test.PassThroughJob;
import repede.alexandru.poc.test.TimeBombJob;
import repede.alexandru.poc.test.TimeConsumingJob;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(JUnit4.class)
public class SchedulingParallelRunnerTest {

    public static final double EXTRA_TIME = 1.1;

    interface Delays {
        int INSTANT = 10;
        int SMALL = 1000;
        int DOUBLE = 2000;
    }
    private final Waiter waiter = new Waiter();
    private final Waiter interruptionWaiter = new Waiter();
    private final Runnable NOTIFY_WAITER_OF_SUCCESS = () -> waiter.resume();
    private final Runnable NOTIFY_WAITER_OF_INTERRUPTION = () -> interruptionWaiter.resume();

    @Test
    public void shouldRun1PassThroughJobInstantly() throws Exception {
        new SchedulingParallelRunner(new RunnableJob[] {new PassThroughJob(NOTIFY_WAITER_OF_SUCCESS)}).execute();
        waiter.await(Delays.INSTANT);
    }

    @Test
    public void shouldRun10PassThroughJobInstantly() throws Exception {
        final int EXPECTED_NUMBER_OF_JOBS = 10;
        List<RunnableJob> jobs = IntStream.rangeClosed(1, EXPECTED_NUMBER_OF_JOBS)
                .mapToObj((counter) -> new PassThroughJob(NOTIFY_WAITER_OF_SUCCESS))
                .collect(Collectors.toList());

        new SchedulingParallelRunner(jobs.toArray(new RunnableJob[EXPECTED_NUMBER_OF_JOBS])).execute();
        long waitingTime = Delays.INSTANT * EXPECTED_NUMBER_OF_JOBS;
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime, EXPECTED_NUMBER_OF_JOBS);
    }

    @Test
    public void shouldRunTimeConsumingJobFastEnough() throws Exception {
        new SchedulingParallelRunner(new RunnableJob[] { new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS) }).execute();
        long waitingTime = (long) (Delays.SMALL * EXTRA_TIME);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime);
    }

    @Test(expected = TimeoutException.class)
    public void shouldNotHaveTimeToRunTimeConsumingJob() throws Exception {
        new SchedulingParallelRunner(new RunnableJob[] { new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS) }).execute();
        waiter.await(Delays.INSTANT);
    }

    @Test(expected = TimeoutException.class)
    public void shouldNotHaveTimeToRun5TimeConsumingJobsOnOneThread() throws Exception {
        final int EXPECTED_NUMBER_OF_JOBS = 5;
        List<IJob> jobs = IntStream.rangeClosed(1, EXPECTED_NUMBER_OF_JOBS)
                .mapToObj((counter) -> new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS))
                .collect(Collectors.toList());

        new SchedulingParallelRunner(jobs.toArray(new RunnableJob[EXPECTED_NUMBER_OF_JOBS])).execute();
        long waitingTime = (long) (Delays.SMALL * EXTRA_TIME);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime, EXPECTED_NUMBER_OF_JOBS);
    }

    @Test
    public void shouldFinishRunning6TimeConsumingJobInFullParallel() throws Exception {
        final int EXPECTED_NUMBER_OF_JOBS = 6;
        List<IJob> jobs = IntStream.rangeClosed(1, EXPECTED_NUMBER_OF_JOBS)
                .mapToObj((counter) -> new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS))
                .collect(Collectors.toList());

        final int PARALLEL_RUNS = 6;
        new SchedulingParallelRunner(PARALLEL_RUNS, jobs.toArray(new RunnableJob[EXPECTED_NUMBER_OF_JOBS])).execute();
        long waitingTime = (long) (Delays.SMALL * EXTRA_TIME);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime, EXPECTED_NUMBER_OF_JOBS);
    }

    @Test
    public void shouldHaveTimeToRun2Of6TimeConsumingJobsOn2Threads() throws Exception {
        final int EXPECTED_NUMBER_OF_JOBS = 6;
        List<IJob> jobs = IntStream.rangeClosed(1, EXPECTED_NUMBER_OF_JOBS)
                .mapToObj((counter) -> new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS))
                .collect(Collectors.toList());

        int NUMBER_OF_PARALLEL_RUNS = 2;
        new SchedulingParallelRunner(NUMBER_OF_PARALLEL_RUNS, jobs.toArray(new RunnableJob[EXPECTED_NUMBER_OF_JOBS])).execute();
        long waitingTime = (long) (Delays.SMALL * EXTRA_TIME);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime, NUMBER_OF_PARALLEL_RUNS);
    }

    @Test(expected = TimeoutException.class)
    public void shouldNotHaveTimeToRun3Of6TimeConsumingJobsOn2Threads() throws Exception {
        final int EXPECTED_NUMBER_OF_JOBS = 6;
        List<IJob> jobs = IntStream.rangeClosed(1, EXPECTED_NUMBER_OF_JOBS)
                .mapToObj((counter) -> new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS))
                .collect(Collectors.toList());

        int NUMBER_OF_PARALLEL_RUNS = 2;
        new SchedulingParallelRunner(NUMBER_OF_PARALLEL_RUNS, jobs.toArray(new RunnableJob[EXPECTED_NUMBER_OF_JOBS])).execute();
        long waitingTime = (long) (Delays.SMALL * EXTRA_TIME);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime, 3);
    }

    @Test
    public void shouldJustContainTheErrorFromTheTimeBombJob() throws Exception {
        new SchedulingParallelRunner(new RunnableJob[] { new TimeBombJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS) }).execute();
        long waitingTime = (long) (Delays.SMALL * EXTRA_TIME);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime);
    }

    @Test
    public void shouldStopRunNextJobsAfterTheTimeBombJobTriggered() throws Exception {
        RunnableJob[] jobs = {
                new TimeBombJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS),
                new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS, NOTIFY_WAITER_OF_INTERRUPTION)};
        new SchedulingParallelRunner(jobs).execute();
        long waitingTime = (long) (Delays.SMALL * EXTRA_TIME);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime);
        interruptionWaiter.await(waitingTime);
    }

    // playing with the timers is getting to fragile when using a test class that does Thread.sleep()
}
