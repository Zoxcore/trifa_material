import com.zoffcc.applications.trifa.MainActivity;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TEST: Message V3 Buffer Handling
 * Tests the tox_messagev3_get_new_message_id function with various buffer sizes
 * and conditions to ensure proper handling of DirectByteBuffers and random ID generation.
 */
public class TestMessageV3Buffer {

    // TOX_MSGV3_MSGID_LENGTH is typically 32 bytes (256 bits)
    private static final int MSGID_LENGTH = 32;

    public static void run() {
        System.out.println("\n--- Test: Message V3 Buffer Handling ---");
        try {
            // =========================================================================
            // 1. Happy Path: Correctly sized buffer
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 1: Testing correctly sized buffer (" + MSGID_LENGTH + " bytes)...");
            ByteBuffer correctBuffer = ByteBuffer.allocateDirect(MSGID_LENGTH);
            byte[] beforeData = new byte[MSGID_LENGTH];
            correctBuffer.get(beforeData);
            
            int res1 = MainActivity.tox_messagev3_get_new_message_id(correctBuffer);
            JniToxcoreUnitTest.assertCondition("tox_messagev3_get_new_message_id with correct size returns 0 (success)", res1 == 0);
            
            // Verify the buffer was actually filled with random data
            byte[] afterData = new byte[MSGID_LENGTH];
            correctBuffer.position(0);
            correctBuffer.get(afterData);
            
            boolean bufferChanged = !Arrays.equals(beforeData, afterData);
            JniToxcoreUnitTest.assertCondition("Buffer was filled with new random data", bufferChanged);
            
            // Verify it's not all zeros (extremely unlikely for random data)
            boolean allZeros = true;
            for (byte b : afterData) {
                if (b != 0) {
                    allZeros = false;
                    break;
                }
            }
            JniToxcoreUnitTest.assertCondition("Generated message ID is not all zeros", !allZeros);

            // =========================================================================
            // 2. Larger buffer than required
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 2: Testing oversized buffer (64 bytes)...");
            ByteBuffer largeBuffer = ByteBuffer.allocateDirect(64);
            int res2 = MainActivity.tox_messagev3_get_new_message_id(largeBuffer);
            JniToxcoreUnitTest.assertCondition("tox_messagev3_get_new_message_id with oversized buffer returns 0 (success)", res2 == 0);

            // =========================================================================
            // 3. Multiple calls generate different IDs
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 3: Testing that multiple calls generate unique IDs...");
            ByteBuffer buf1 = ByteBuffer.allocateDirect(MSGID_LENGTH);
            ByteBuffer buf2 = ByteBuffer.allocateDirect(MSGID_LENGTH);
            
            MainActivity.tox_messagev3_get_new_message_id(buf1);
            MainActivity.tox_messagev3_get_new_message_id(buf2);
            
            byte[] id1 = new byte[MSGID_LENGTH];
            byte[] id2 = new byte[MSGID_LENGTH];
            buf1.position(0);
            buf1.get(id1);
            buf2.position(0);
            buf2.get(id2);
            
            boolean idsAreDifferent = !Arrays.equals(id1, id2);
            JniToxcoreUnitTest.assertCondition("Two consecutive calls generate different message IDs", idsAreDifferent);

            // =========================================================================
            // 4. Non-direct ByteBuffer (should fail gracefully)
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 4: Testing non-direct ByteBuffer...");
            ByteBuffer nonDirectBuffer = ByteBuffer.allocate(MSGID_LENGTH);
            try {
                int res4 = MainActivity.tox_messagev3_get_new_message_id(nonDirectBuffer);
                // If it doesn't crash, it should return an error code
                JniToxcoreUnitTest.assertCondition("Non-direct ByteBuffer handled without crash (returned " + res4 + ")", true);
            } catch (Throwable e) {
                JniToxcoreUnitTest.assertCondition("Non-direct ByteBuffer threw exception: " + e.getClass().getSimpleName(), false);
            }

            // =========================================================================
            // 5. Concurrent stress test
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 5: Concurrent stress test (20 threads x 100 calls)...");
            int numThreads = 20;
            int callsPerThread = 100;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < numThreads; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < callsPerThread; j++) {
                            ByteBuffer threadBuffer = ByteBuffer.allocateDirect(MSGID_LENGTH);
                            int res = MainActivity.tox_messagev3_get_new_message_id(threadBuffer);
                            if (res != 0) {
                                errorCount.incrementAndGet();
                            }
                        }
                    } catch (Throwable t) {
                        errorCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            boolean finished = doneLatch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            
            JniToxcoreUnitTest.assertCondition("Concurrent stress test finished within timeout", finished);
            JniToxcoreUnitTest.assertCondition("Concurrent stress test had zero errors", errorCount.get() == 0);

            // =========================================================================
            // 6. Rapid sequential calls
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 6: Testing 1000 rapid sequential calls...");
            int successCount = 0;
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                ByteBuffer rapidBuffer = ByteBuffer.allocateDirect(MSGID_LENGTH);
                int res = MainActivity.tox_messagev3_get_new_message_id(rapidBuffer);
                if (res == 0) {
                    successCount++;
                }
            }
            long elapsed = System.currentTimeMillis() - startTime;
            
            JniToxcoreUnitTest.assertCondition("1000 rapid sequential calls all succeeded", successCount == 1000);
            System.out.println("\u001B[90m[INFO]\u001B[0m 1000 calls completed in " + elapsed + "ms (" + 
                             String.format("%.2f", 1000.0 / elapsed * 1000) + " calls/sec)");

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("Message V3 Buffer test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}

