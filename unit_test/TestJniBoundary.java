import com.zoffcc.applications.trifa.MainActivity;
import java.nio.ByteBuffer;

/**
 * TEST: JNI Boundary & Security Validation
 * Tests that the C code safely handles invalid inputs (wrong buffer sizes, bad strings)
 * and returns appropriate error codes without causing segmentation faults.
 */
public class TestJniBoundary {

    public static void run() {
        System.out.println("\n--- Test: JNI Boundary & Security Validation ---");
        try {
            // 1. Test tox_messagev3_get_new_message_id with a buffer that is too small.
            // The C code expects a buffer of at least TOX_MSGV3_MSGID_LENGTH (usually 32 bytes).
            // Passing a 10-byte buffer should trigger the capacity check and return -2.
            ByteBuffer smallBuffer = ByteBuffer.allocateDirect(10);
            int res1 = MainActivity.tox_messagev3_get_new_message_id(smallBuffer);
            JniToxcoreUnitTest.assertCondition("tox_messagev3_get_new_message_id rejects too small buffer (returns -2)", res1 == -2);

            // 2. Test tox_friend_add with an invalid/too short ToxID string.
            // A valid ToxID is 76 hex characters. Passing a 10-character string should fail gracefully.
            long res2 = MainActivity.tox_friend_add("1234567890", "Hello");
            JniToxcoreUnitTest.assertCondition("tox_friend_add rejects invalid/too short toxid (returns < 0)", res2 < 0);

            // 3. Test tox_file_send with a null ByteBuffer.
            // The C code should handle the null pointer safely and return an error code.
            long res3 = MainActivity.tox_file_send(0L, 0L, 100L, null, "test.txt", 8L);
            JniToxcoreUnitTest.assertCondition("tox_file_send handles null ByteBuffer without crashing (returns < 0)", res3 < 0);

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("JNI Boundary test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
