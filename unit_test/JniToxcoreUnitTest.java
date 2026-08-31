import com.zoffcc.applications.trifa.MainActivity;

/**
 * JniToxcore Plain Java Unit Test Runner
 */
public class JniToxcoreUnitTest {

    public static int passed = 0;
    public static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" JniToxcore Plain Java Unit Tests");
        System.out.println("========================================\n");

        try {
            System.loadLibrary("jni-c-toxcore");
            System.out.println("\u001B[32m[PASS]\u001B[0m Successfully loaded libjni-c-toxcore");
        } catch (UnsatisfiedLinkError e) {
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
            System.out.println("\n\u001B[90m[INFO]\u001B[0m Starting Tox background iteration thread...");
            ToxIterateRunner.start();

            // Optional: Give Tox a brief moment to stabilize
            Thread.sleep(500);

            // 4. Bootstrap from network nodes and wait to come online
            TestBootstrap.run();

            // 5. Thread pounding test to verify JNI/Toxcore thread safety under load
            TestThreadPounding.run();

            // =============================================
            // NEW CRITICAL TESTS
            // =============================================

            // 6. JNI Boundary & Security Validation
            TestJniBoundary.run();

            // 7. Friend Management Edge Cases
            TestFriendEdgeCases.run();

            // 8. Savedata Persistence & Disk I/O
            TestSavedataPersistence.run();

            // 9. Self Profile, State & ToxID Generation
            TestSelfProfile.run();

        } catch (Throwable t) {
            System.out.println("\u001B[31m[FAIL]\u001B[0m Test suite crashed: " + t.getMessage());
            t.printStackTrace();
            failed++;
        } finally {
            // =============================================
            // Cleanup
            // =============================================

            System.out.println("\n\u001B[90m[INFO]\u001B[0m Stopping Tox background iteration thread...");
            ToxIterateRunner.stop();

            System.out.println("\u001B[90m[INFO]\u001B[0m Calling tox_kill() for cleanup...");
            try {
                MainActivity.tox_kill();
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

    public static void assertConditionNoColor(String testName, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            failed++;
        }
    }

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
