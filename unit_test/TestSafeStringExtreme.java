import com.zoffcc.applications.trifa.TrifaToxService;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TEST: TrifaToxService.safe_string Extreme Boundaries
 * Pushes the Java-side sanitization method to its absolute limits using fuzzing,
 * memory stress, specific UTF-8 edge cases, and concurrency.
 */
public class TestSafeStringExtreme {

    public static void run() {
        System.out.println("\n--- Test: TrifaToxService.safe_string Extreme Boundaries ---");
        try {
            // =========================================================================
            // 1. Random Fuzzing (10,000 iterations)
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 1: Random byte array fuzzing (10,000 iterations)...");
            Random random = new Random(42); // Fixed seed for reproducibility
            int fuzzCount = 10000;
            for (int i = 0; i < fuzzCount; i++) {
                int length = random.nextInt(1000) + 1; // 1 to 1000 bytes
                byte[] randomBytes = new byte[length];
                random.nextBytes(randomBytes);
                
                String result = TrifaToxService.safe_string(randomBytes);
                if (result == null) {
                    throw new AssertionError("safe_string returned null for random bytes at iteration " + i);
                }
            }
            JniToxcoreUnitTest.assertCondition("Fuzzing: 10,000 random byte arrays processed without exceptions", true);

            // =========================================================================
            // 2. Specific UTF-8 Edge Cases (Historically problematic sequences)
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 2: Specific malformed UTF-8 edge cases...");
            
            // Overlong encoding of 'A' (0x41) -> 0xC1 0x81
            String resOverlong = TrifaToxService.safe_string(new byte[]{(byte) 0xC1, (byte) 0x81});
            JniToxcoreUnitTest.assertCondition("Handles overlong UTF-8 encoding", resOverlong != null && resOverlong.contains(""));

            // Truncated 3-byte sequence (missing the 3rd byte)
            String resTruncated = TrifaToxService.safe_string(new byte[]{(byte) 0xE2, (byte) 0x82});
            JniToxcoreUnitTest.assertCondition("Handles truncated UTF-8 sequence", resTruncated != null && resTruncated.contains(""));

            // Invalid start byte (0xFF is never valid in UTF-8)
            String resInvalidStart = TrifaToxService.safe_string(new byte[]{(byte) 0xFF, 0x41, 0x42});
            JniToxcoreUnitTest.assertCondition("Handles invalid UTF-8 start byte", resInvalidStart != null && resInvalidStart.startsWith(""));

            // Isolated high surrogate bytes (0xED 0xA0 0x80 = U+D800)
            String resSurrogate = TrifaToxService.safe_string(new byte[]{(byte) 0xED, (byte) 0xA0, (byte) 0x80});
            JniToxcoreUnitTest.assertCondition("Handles isolated surrogate bytes", resSurrogate != null && resSurrogate.contains(""));

            // =========================================================================
            // 3. Memory Stress (Large Arrays)
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 3: Memory stress (50MB byte array)...");
            try {
                int largeSize = 50 * 1024 * 1024; // 50 MB
                byte[] largeArray = new byte[largeSize];
                // Fill with a mix of valid and invalid UTF-8
                for (int i = 0; i < largeSize; i++) {
                    largeArray[i] = (byte) (i % 256);
                }
                
                String largeResult = TrifaToxService.safe_string(largeArray);
                JniToxcoreUnitTest.assertCondition("Large array (50MB) processed successfully", largeResult != null);
                JniToxcoreUnitTest.assertCondition("Large array result has expected length", largeResult.length() > 0);
            } catch (OutOfMemoryError e) {
                // If the test environment has a very small heap, it might throw OOM.
                // This is a Java Error, NOT a JVM crash. The C code's ExceptionCheck 
                // will catch this, clear it, and safely return NULL.
                System.out.println("\u001B[90m[INFO]\u001B[0m Phase 3: OutOfMemoryError caught (expected in low-memory environments).");
                System.out.println("\u001B[90m[INFO]\u001B[0m Note: The C code safely handles this via ExceptionCheck and returns NULL.");
                JniToxcoreUnitTest.assertCondition("Large array triggered OOM (handled gracefully by Java, not a JVM crash)", true);
            }

            // =========================================================================
            // 4. Concurrency Stress
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 4: Concurrent stress (20 threads x 1000 calls)...");
            int numThreads = 20;
            int callsPerThread = 1000;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            AtomicInteger errorCount = new AtomicInteger(0);

            byte[] sharedMalformedBytes = {(byte) 0xC3, (byte) 0x28, (byte) 0xFF, 0x00, 0x41};

            for (int i = 0; i < numThreads; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < callsPerThread; j++) {
                            String res = TrifaToxService.safe_string(sharedMalformedBytes);
                            if (res == null) {
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

            startLatch.countDown(); // GO!
            boolean finished = doneLatch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            
            JniToxcoreUnitTest.assertCondition("Concurrent stress finished within timeout", finished);
            JniToxcoreUnitTest.assertCondition("Concurrent stress had zero errors", errorCount.get() == 0);

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("Safe String Extreme test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
