package repede.alexandru.poc;

import net.jodah.concurrentunit.Waiter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(JUnit4.class)
public class SchedulingParallelRunnerTest {
    interface Delays {
        int INSTANT = 10;
        int SMALL = 1000;
    }
    private final Waiter waiter = new Waiter();
    private final Runnable NOTIFY_WAITER_OF_SUCCESS = () -> waiter.resume();

    @Test
    public void shouldRun1PassThroughJobInstantly() throws Exception {
        new SchedulingParallelRunner(new IJob[] {new PassThroughJob(NOTIFY_WAITER_OF_SUCCESS)}).execute();
        waiter.await(Delays.INSTANT);
    }
    @Test
    public void shouldRun10PassThroughJobInstantly() throws Exception {
        final int EXPECTED_NUMBER_OF_JOBS = 10;
        List<IJob> jobs = IntStream.rangeClosed(1, EXPECTED_NUMBER_OF_JOBS)
                .mapToObj((counter) -> new PassThroughJob(NOTIFY_WAITER_OF_SUCCESS))
                .collect(Collectors.toList());

        new SchedulingParallelRunner(jobs.toArray(new IJob[EXPECTED_NUMBER_OF_JOBS])).execute();
        waiter.await(Delays.INSTANT * EXPECTED_NUMBER_OF_JOBS, EXPECTED_NUMBER_OF_JOBS);
    }

    @Test
    public void shouldRunTimeConsumingJobFastEnough() throws Exception {
        new SchedulingParallelRunner(new IJob[] { new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS) }).execute();
        waiter.await((long) (Delays.SMALL * 1.1));
    }

    @Test(expected = TimeoutException.class)
    public void shouldNotHaveTimeToRunTimeConsumingJob() throws Exception {
        new SchedulingParallelRunner(new IJob[] { new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS) }).execute();
        waiter.await(Delays.INSTANT);
    }

    // TODO : this bastard should not pass because the pool has 1 thread and 1.1s to run its jobs, but it is asked to run for 3s from 3 jobs
    @Test(expected = TimeoutException.class)
    public void shouldNotHaveTimeToRun3TimeConsumingJobs() throws Exception {
        final int EXPECTED_NUMBER_OF_JOBS = 3;
        List<IJob> jobs = IntStream.rangeClosed(1, EXPECTED_NUMBER_OF_JOBS)
                .mapToObj((counter) -> new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS))
                .collect(Collectors.toList());

        new SchedulingParallelRunner(jobs.toArray(new IJob[EXPECTED_NUMBER_OF_JOBS])).execute();
        long waitingTime = (long) (Delays.SMALL * 1.1);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await((long) (Delays.SMALL * 1.1), EXPECTED_NUMBER_OF_JOBS);
    }

    // TODO : this passes because all jobs are run in parallel (so the requirements are not met yet: must use a limited pool, but still this test is ok)
    @Test
    public void shouldRunTime6ConsumingJobInParallel() throws Exception {
        final int EXPECTED_NUMBER_OF_JOBS = 6;
        List<IJob> jobs = IntStream.rangeClosed(1, EXPECTED_NUMBER_OF_JOBS)
                .mapToObj((counter) -> new TimeConsumingJob(Delays.SMALL, NOTIFY_WAITER_OF_SUCCESS))
                .collect(Collectors.toList());

        final int PARALLEL_RUNS = 6;
        new SchedulingParallelRunner(PARALLEL_RUNS, jobs.toArray(new IJob[EXPECTED_NUMBER_OF_JOBS])).execute();
        long waitingTime = (long) (Delays.SMALL * 1.1);
        System.out.format("\nwaiting : %d", waitingTime);
        waiter.await(waitingTime, EXPECTED_NUMBER_OF_JOBS);
    }

    @Test(expected = TimeoutException.class)
    public void shouldFailBecauseItRunToLong () throws Exception {
        //Waiter waiter2 = new Waiter();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Delays.SMALL);
                    waiter.resume();
                    waiter.resume();
                } catch (InterruptedException e) {
                    throw new RuntimeException("I was killed");
                }
            }
        }).start();
        waiter.await(Delays.INSTANT,2);
    }

    @Test
    public void shouldNotFailBecauseItHadTimeToRun () throws Exception {
        //Waiter waiter2 = new Waiter();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Delays.INSTANT);
                    waiter.resume();
                    waiter.resume();
                } catch (InterruptedException e) {
                    throw new RuntimeException("I was killed");
                }
            }
        }).start();
        waiter.await(Delays.SMALL ,2);
    }
}
