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
public class WaiterPOCTest {
    interface Delays {
        int INSTANT = 10;
        int SMALL = 1000;
    }
    private final Waiter waiter = new Waiter();

    @Test(expected = TimeoutException.class)
    public void shouldFailBecauseItRunToLong () throws Exception {
        //Waiter localWaiter = new Waiter();
        new Thread(() -> {
            try {
                Thread.sleep(Delays.SMALL);
                waiter.resume();
                waiter.resume();
            } catch (InterruptedException e) {
                throw new RuntimeException("I was killed");
            }
        }).start();
        waiter.await(Delays.INSTANT,2);
    }

    @Test
    public void shouldNotFailBecauseItHadTimeToRun () throws Exception {
        //Waiter waiter2 = new Waiter();
        new Thread(() -> {
            try {
                Thread.sleep(Delays.INSTANT);
                waiter.resume();
                waiter.resume();
            } catch (InterruptedException e) {
                throw new RuntimeException("I was killed");
            }
        }).start();
        waiter.await(Delays.SMALL ,2);
    }
}
