package nl.flotsam.spring.integration.kaha;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger {

    private CountDownLatch latch;
    private long start;
    private AtomicBoolean started;

    public Logger(int count) {
        latch = new CountDownLatch(count);
        started = new AtomicBoolean(false);
    }

    public void receive(String message) {
        if (started.compareAndSet(false, true)) {
            System.out.println("Starting at " + System.currentTimeMillis());
            start = System.currentTimeMillis();
        }
        latch.countDown();
    }

    public long waitForCompletion() throws InterruptedException {
        latch.await();
        System.out.println("Stopping at " + System.currentTimeMillis());
        return System.currentTimeMillis() - start;
    }

}
