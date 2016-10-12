package repede.alexandru.poc;

public class PassThroughJob implements IJob {


    private final Runnable after;

    public PassThroughJob(Runnable after) {
        this.after = after;
    }

    @Override
    public void execute() {
        after.run();
    }
}
