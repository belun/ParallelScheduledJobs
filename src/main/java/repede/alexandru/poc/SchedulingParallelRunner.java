package repede.alexandru.poc;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SchedulingParallelRunner implements IJob {

    private final List<RunnableJob> jobs;
    // this needs to be some blocking collection if this class is going to be used concurrently itself

    private ExecutorService executor;
    // no idea yet what happens here if the execute() of this Runner is called concurrently (might need to just synchronize this)
    // some people say that your jobs need to be thread safe, not the Runner
    // the thread safety of the Runner needs proper testing


    private ExceptionHandler exceptionHandler = new ExceptionHandler(this);

    public SchedulingParallelRunner(RunnableJob... jobs) {
        this(1, jobs);
    }

    public SchedulingParallelRunner(int numberOfParallelRuns, RunnableJob... jobs) {
        this.jobs = Arrays.asList(jobs);
        this.executor = Executors.newFixedThreadPool(numberOfParallelRuns, new HandledThreadFactory(exceptionHandler));
    }

    public void execute() {
        jobs.stream().forEach(job -> executor.execute(job));
        executor.shutdown();
    }

    public void shutDown() {
        executor.shutdownNow();
    }

    private static class HandledThreadFactory implements ThreadFactory {
        private final ExceptionHandler exceptionHandler;

        private HandledThreadFactory(ExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        }
    }
}
