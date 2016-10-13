package repede.alexandru.poc;


public abstract class RunnableJob implements IJob, Runnable {

    public abstract void execute();

    @Override
    public void run() {
        execute();
    }
}
