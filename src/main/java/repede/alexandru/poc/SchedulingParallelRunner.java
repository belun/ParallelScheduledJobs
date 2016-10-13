package repede.alexandru.poc;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SchedulingParallelRunner implements IJob {

    private final List<RunnableJob> jobs;
    // this needs to be some blocking collection if this class is going to be used concurrently itself

    private ExecutorService executor;
    // no idea yet what happens here if the execute() of this Runner is called concurrently (might need to just synchronize this)
    // some people say that your jobs need to be thread safe, not the Runner
    // the thread safety of the Runner needs proper testing

    public SchedulingParallelRunner(RunnableJob... jobs) {
        this(1, jobs);
    }

    public SchedulingParallelRunner(int numberOfParallelRuns, RunnableJob... jobs) {
        this.jobs = Arrays.asList(jobs);
        this.executor = Executors.newFixedThreadPool(numberOfParallelRuns);
    }

    public void execute() {
        jobs.stream().forEach(job -> executor.execute(job));
    }
}
