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
            System.out.println("[PASS] Successfully loaded libjni-c-toxcore.so");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("[FAIL] Failed to load libjni-c-toxcore.so");
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }

        // =============================================
        // Run all test suites
        // =============================================
        
        // 1. Test basic JNI bindings before init
        TestJniToxcore.run();
        
        // 2. Test full Tox initialization
        TestToxInit.run();

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
