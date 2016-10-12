package repede.alexandru.poc;

import java.util.Arrays;
import java.util.List;

public class SchedulingParallelRunner implements IJob {

    private final int numberOfParallelRuns;
    private final List<IJob> jobs;

    public SchedulingParallelRunner(IJob... jobs) {
        this(1, jobs);
    }

    public SchedulingParallelRunner(int numberOfParallelRuns, IJob... jobs) {
        this.numberOfParallelRuns = numberOfParallelRuns;
        this.jobs = Arrays.asList(jobs);
    }

    public void execute() {
        jobs.stream().forEach(job -> new Thread(() -> job.execute()).start());
    }
}
