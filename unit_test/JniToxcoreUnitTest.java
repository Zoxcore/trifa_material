import com.zoffcc.applications.trifa.MainActivity;

/**
 * JniToxcore Plain Java Unit Test Runner
 *
 * This is the main entry point. It initializes the JNI library,
 * runs all test suites, and prints the final summary.
 * Common helper methods (assertCondition) are defined here.
 */
public class JniToxcoreUnitTest {

    // Global test result counters (accessible from all test classes)
    public static int passed = 0;
    public static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" JniToxcore Plain Java Unit Tests");
        System.out.println("========================================\n");

        try {
            System.loadLibrary("jni-c-toxcore");
            // Added ANSI green color code
            System.out.println("\u001B[32m[PASS]\u001B[0m Successfully loaded libjni-c-toxcore");
        } catch (UnsatisfiedLinkError e) {
            // Added ANSI red color code
            System.out.println("\u001B[31m[FAIL]\u001B[0m Failed to load libjni-c-toxcore");
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }

        try {
            // =============================================
            // Run all test suites
            // =============================================
            
            // 1. Test basic JNI bindings before init
            TestJniToxcore.run();
            
            // 2. Test full Tox initialization
            TestToxInit.run();

            // 3. Start background iteration thread so Tox can process events
            // (Started after init so the core is actually alive and needs iterating)
            System.out.println("\n\u001B[90m[INFO]\u001B[0m Starting Tox background iteration thread...");
            ToxIterateRunner.start();

            // Optional: Give Tox a brief moment to stabilize (e.g., initial bootstrap steps)
            Thread.sleep(500);

            // TODO: Future tests (e.g., TestMessaging.run(), TestConferences.run()) 
            // can be added here. They will execute while tox_iterate() is actively running.

        } catch (Throwable t) {
            // Added ANSI red color code
            System.out.println("\u001B[31m[FAIL]\u001B[0m Test suite crashed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        } finally {
            // =============================================
            // Cleanup
            // =============================================
            
            // 4. Stop the background thread
            System.out.println("\n\u001B[90m[INFO]\u001B[0m Stopping Tox background iteration thread...");
            ToxIterateRunner.stop();

            // 5. Clean up Tox core globally
            System.out.println("\u001B[90m[INFO]\u001B[0m Calling tox_kill() for cleanup...");
            try {
                MainActivity.tox_kill();
                // Use the ANSI escape codes directly so it prints in green 
                // without incrementing the 'passed' test counter.
                System.out.println("\u001B[32m[PASS]\u001B[0m tox_kill() completed successfully.");
            } catch (Throwable t) {
                System.out.println("\u001B[31m[FAIL]\u001B[0m tox_kill() threw an exception: " + t.getMessage());
                failed++;
            }

            // Print final summary
            System.out.println("\n========================================");
            System.out.println(" TEST SUMMARY");
            System.out.println("========================================");
            System.out.println("Passed: " + passed);
            System.out.println("Failed: " + failed);
            if (failed > 0) {
                System.exit(1);
            }
        }
    }

    // =========================================================================
    // HELPER: Simple assertion without external libraries
    // Prints [PASS] or [FAIL] and increments global counters.
    // =========================================================================
    public static void assertConditionNoColor(String testName, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            failed++;
        }
    }

    // =========================================================================
    // HELPER: Simple assertion without external libraries
    // Prints [PASS] in green or [FAIL] in red and increments global counters.
    // =========================================================================
    public static void assertCondition(String testName, boolean condition) {
        if (condition) {
            System.out.println("\u001B[32m[PASS]\u001B[0m " + testName);
            passed++;
        } else {
            System.out.println("\u001B[31m[FAIL]\u001B[0m " + testName);
            failed++;
        }
    }
}
