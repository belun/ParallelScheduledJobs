package repede.alexandru.poc;


class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final SchedulingParallelRunner runner;

    ExceptionHandler(SchedulingParallelRunner runner) {
        this.runner = runner;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        System.out.format("\nThread %s hit by exception %s.", thread.getName(), e.toString());
        runner.shutDown();
        // I have serious doubts about this kind of exception handling:
        // the dependencies are bad : Runner -> Executor -> Handler -> Runner -> Executor
        // Might be time to upgrade Jobs from Runnables to Callables and store their Futures
        // for checking errors and shutdown on demand
    }
}
