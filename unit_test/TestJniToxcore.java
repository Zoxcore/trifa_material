import com.zoffcc.applications.trifa.MainActivity;

/**
 * TEST: JniToxcore Basic
 * Tests basic JNI bindings to ensure the library loads and methods can be called safely.
 */
public class TestJniToxcore {

    public static void run() {
        System.out.println("\n--- Test: JniToxcore Basic ---");
        try {
            MainActivity tox = new MainActivity();

            // Test getting name size
            // If tox_global is NULL in C, it safely returns 0.
            long nameSize = tox.tox_self_get_name_size();
            JniToxcoreUnitTest.assertCondition("tox_self_get_name_size executes without crash", true);
            JniToxcoreUnitTest.assertCondition("tox_self_get_name_size returns >= 0", nameSize >= 0);

            // Test getting name
            String name = tox.tox_self_get_name();
            JniToxcoreUnitTest.assertCondition("tox_self_get_name executes without crash", true);
            JniToxcoreUnitTest.assertCondition("tox_self_get_name returns null when uninitialized", name == null);

            // Test getting connection status for a dummy friend number (e.g., 0)
            // The C code returns TOX_CONNECTION_NONE (0) if tox_global is NULL.
            int connStatus = tox.tox_friend_get_connection_status(0L);
            JniToxcoreUnitTest.assertCondition("tox_friend_get_connection_status executes without crash", true);
            JniToxcoreUnitTest.assertCondition("tox_friend_get_connection_status returns 0 when uninitialized", connStatus == 0);

        } catch (UnsatisfiedLinkError e) {
            JniToxcoreUnitTest.assertCondition("JniToxcore Basic test failed: UnsatisfiedLinkError", false);
            System.err.println("Native library not found. Ensure -Djava.library.path is set correctly.");
            e.printStackTrace();
        } catch (Exception e) {
            JniToxcoreUnitTest.assertCondition("JniToxcore Basic test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
