package repede.alexandru.poc.test;

import repede.alexandru.poc.RunnableJob;

public class PassThroughJob extends RunnableJob {


    private final Runnable after;

    public PassThroughJob(Runnable after) {
        this.after = after;
    }

    @Override
    public void execute() {
        after.run();
    }
}
