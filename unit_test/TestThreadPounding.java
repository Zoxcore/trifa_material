import com.zoffcc.applications.trifa.MainActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TEST: Thread Pounding
 * Tests the thread safety of the JNI bindings and Toxcore by calling
 * various API functions concurrently from multiple threads.
 */
public class TestThreadPounding {

    private static final int NUM_THREADS = 40;
    private static final int ITERATIONS_PER_THREAD = 1000;

    public static void run() {
        System.out.println("\n--- Test: Thread Pounding ---");
        System.out.println("\u001B[90m[INFO]\u001B[0m Spawning " + NUM_THREADS + " threads, each making " + ITERATIONS_PER_THREAD + " API calls...");
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_THREADS);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            Thread t = new Thread(() -> {
                try {
                    // Wait for all threads to be ready to maximize concurrency
                    startLatch.await();
                    
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        // Cycle through various safe API calls to stress test JNI and Toxcore
                        switch (j % 12) {
                            case 0:  MainActivity.tox_self_get_name(); break;
                            case 1:  MainActivity.tox_self_get_name_size(); break;
                            case 2:  MainActivity.tox_self_get_connection_status(); break;
                            case 3:  MainActivity.tox_self_get_friend_list_size(); break;
                            case 4:  MainActivity.tox_self_get_capabilities(); break;
                            case 5:  MainActivity.tox_self_get_nospam(); break;
                            case 6:  MainActivity.tox_version_major(); break;
                            case 7:  MainActivity.tox_version_minor(); break;
                            case 8:  MainActivity.tox_version_patch(); break;
                            case 9:  MainActivity.tox_friend_get_connection_status(0L); break;
                            case 10: MainActivity.tox_friend_get_connection_status(999L); break;
                            case 11: MainActivity.tox_self_set_typing(0L, 0); break;
                        }
                    }
                } catch (Throwable e) {
                    System.err.println("\u001B[31m[ERROR]\u001B[0m Thread " + threadId + " threw exception: " + e.getMessage());
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            }, "Pound-Thread-" + i);
            threads.add(t);
            t.start();
        }

        // Start all threads simultaneously
        startLatch.countDown();

        try {
            // Wait for all threads to finish (with a generous timeout)
            boolean finished = doneLatch.await(30, java.util.concurrent.TimeUnit.SECONDS);
            
            if (!finished) {
                System.err.println("\u001B[31m[FAIL]\u001B[0m Thread pounding test timed out.");
                JniToxcoreUnitTest.assertCondition("Thread pounding completed within timeout", false);
            } else {
                int errors = errorCount.get();
                if (errors > 0) {
                    System.err.println("\u001B[31m[FAIL]\u001B[0m " + errors + " threads encountered errors.");
                    JniToxcoreUnitTest.assertCondition("Thread pounding completed without errors", false);
                } else {
                    System.out.println("\u001B[90m[INFO]\u001B[0m All " + NUM_THREADS + " threads completed successfully.");
                    JniToxcoreUnitTest.assertCondition("Thread pounding completed without errors", true);
                    JniToxcoreUnitTest.assertCondition("Thread pounding completed within timeout", true);
                }
            }
        } catch (InterruptedException e) {
            JniToxcoreUnitTest.assertCondition("Thread pounding test interrupted", false);
            Thread.currentThread().interrupt();
        }
    }
}
