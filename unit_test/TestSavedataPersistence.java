import com.zoffcc.applications.trifa.MainActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TEST: Savedata Persistence & Disk I/O
 * Verifies that the JNI layer correctly writes the Tox savedata file to the 
 * filesystem using the data_dir provided during initialization.
 * 
 * Also stress-tests thread safety by pounding the save function from 50 concurrent threads.
 */
public class TestSavedataPersistence {

    private static final int NUM_THREADS = 50;
    private static final int CALLS_PER_THREAD = 100;

    public static void run() {
        System.out.println("\n--- Test: Savedata Persistence & Disk I/O ---");
        try {
            String dataDir = TestToxInit.testDataDir;
            String passphraseHash = TestToxInit.testPassphraseHash;

            if (dataDir == null) {
                JniToxcoreUnitTest.assertCondition("Savedata test skipped: testDataDir is null", false);
                return;
            }

            // =========================================================================
            // PHASE 1: Basic single-threaded save
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 1: Calling update_savedata_file (single call)...");
            MainActivity.update_savedata_file(passphraseHash);
            JniToxcoreUnitTest.assertCondition("update_savedata_file executes without crash", true);

            File savedataFile = new File(dataDir, "savedata.tox");
            boolean exists = savedataFile.exists();
            JniToxcoreUnitTest.assertCondition("savedata.tox file exists on disk after single save", exists);

            long sizeAfterSingleSave = 0;
            if (exists) {
                sizeAfterSingleSave = savedataFile.length();
                System.out.println("\u001B[90m[INFO]\u001B[0m savedata.tox size after single save: " + sizeAfterSingleSave + " bytes");
                JniToxcoreUnitTest.assertCondition("savedata.tox file size > 0 after single save", sizeAfterSingleSave > 0);
            }

            // =========================================================================
            // PHASE 2: Thread Pounding - 50 threads x 100 calls = 5000 concurrent saves
            // =========================================================================
            System.out.println("\n\u001B[90m[INFO]\u001B[0m Phase 2: Thread Pounding - " + NUM_THREADS + " threads x " + CALLS_PER_THREAD + " calls each...");
            System.out.println("\u001B[90m[INFO]\u001B[0m Total: " + (NUM_THREADS * CALLS_PER_THREAD) + " concurrent save operations...");

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(NUM_THREADS);
            AtomicInteger errorCount = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            List<Thread> threads = new ArrayList<>();

            long poundStartTime = System.currentTimeMillis();

            for (int i = 0; i < NUM_THREADS; i++) {
                final int threadId = i;
                Thread t = new Thread(() -> {
                    try {
                        // Wait for all threads to be ready to maximize concurrency
                        startLatch.await();

                        for (int j = 0; j < CALLS_PER_THREAD; j++) {
                            try {
                                MainActivity.update_savedata_file(passphraseHash);
                                successCount.incrementAndGet();
                            } catch (Throwable e) {
                                errorCount.incrementAndGet();
                                if (errorCount.get() <= 3) {
                                    // Only print first 3 errors to avoid spam
                                    System.err.println("\u001B[31m[ERROR]\u001B[0m Thread " + threadId + " iteration " + j + " threw: " + e.getMessage());
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable e) {
                        errorCount.incrementAndGet();
                        System.err.println("\u001B[31m[ERROR]\u001B[0m Thread " + threadId + " setup failed: " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                }, "Save-Pound-Thread-" + i);
                threads.add(t);
                t.start();
            }

            // Release all threads simultaneously for maximum contention
            startLatch.countDown();

            // Wait for all threads to finish (with a generous 2-minute timeout)
            boolean finished = doneLatch.await(120, java.util.concurrent.TimeUnit.SECONDS);
            long poundElapsed = System.currentTimeMillis() - poundStartTime;

            if (!finished) {
                System.err.println("\u001B[31m[FAIL]\u001B[0m Thread pounding test timed out after 120 seconds.");
                JniToxcoreUnitTest.assertCondition("Savedata thread pounding completed within timeout", false);
            } else {
                int errors = errorCount.get();
                int successes = successCount.get();
                System.out.println("\u001B[90m[INFO]\u001B[0m Thread pounding completed in " + poundElapsed + "ms");
                System.out.println("\u001B[90m[INFO]\u001B[0m Successful saves: " + successes + " / " + (NUM_THREADS * CALLS_PER_THREAD));
                System.out.println("\u001B[90m[INFO]\u001B[0m Failed saves: " + errors);

                JniToxcoreUnitTest.assertCondition("Savedata thread pounding completed within timeout", true);
                JniToxcoreUnitTest.assertCondition("All " + (NUM_THREADS * CALLS_PER_THREAD) + " saves succeeded without errors", errors == 0);
            }

            // =========================================================================
            // PHASE 3: Verify file integrity after pounding
            // =========================================================================
            System.out.println("\n\u001B[90m[INFO]\u001B[0m Phase 3: Verifying file integrity after pounding...");

            // Force a final single save to ensure clean state
            MainActivity.update_savedata_file(passphraseHash);

            boolean existsAfterPound = savedataFile.exists();
            JniToxcoreUnitTest.assertCondition("savedata.tox file still exists after pounding", existsAfterPound);

            if (existsAfterPound) {
                long sizeAfterPound = savedataFile.length();
                System.out.println("\u001B[90m[INFO]\u001B[0m savedata.tox size after pounding: " + sizeAfterPound + " bytes");
                
                JniToxcoreUnitTest.assertCondition("savedata.tox file size > 0 after pounding", sizeAfterPound > 0);
                
                // Verify the file wasn't corrupted to an absurdly small size
                JniToxcoreUnitTest.assertCondition("savedata.tox file size is reasonable (> 100 bytes)", sizeAfterPound > 100);
                
                // Verify the file size is roughly consistent (not 10x larger or smaller due to race conditions)
                if (sizeAfterSingleSave > 0) {
                    double ratio = (double) sizeAfterPound / (double) sizeAfterSingleSave;
                    boolean sizeConsistent = ratio > 0.5 && ratio < 2.0;
                    JniToxcoreUnitTest.assertCondition("savedata.tox file size is consistent after pounding (ratio: " + String.format("%.2f", ratio) + ")", sizeConsistent);
                }
            }

            // =========================================================================
            // PHASE 4: Verify Tox core is still functional after pounding
            // =========================================================================
            System.out.println("\n\u001B[90m[INFO]\u001B[0m Phase 4: Verifying Tox core still functional after pounding...");
            
            String nameAfterPound = MainActivity.tox_self_get_name();
            JniToxcoreUnitTest.assertCondition("tox_self_get_name still works after pounding", nameAfterPound != null);
            JniToxcoreUnitTest.assertCondition("tox_self_get_name returns correct value after pounding", "JniTestUser".equals(nameAfterPound));
            
            long friendListSize = MainActivity.tox_self_get_friend_list_size();
            JniToxcoreUnitTest.assertCondition("tox_self_get_friend_list_size still works after pounding", friendListSize >= 0);

            int connStatus = MainActivity.tox_self_get_connection_status();
            JniToxcoreUnitTest.assertCondition("tox_self_get_connection_status still works after pounding", connStatus >= 0);

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("Savedata Persistence test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
