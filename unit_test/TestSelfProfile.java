import com.zoffcc.applications.trifa.MainActivity;

/**
 * TEST: Self Profile, State & ToxID Generation
 * Tests that the core cryptographic identity functions work correctly, 
 * including ToxID generation, status messages, and nospam modification.
 */
public class TestSelfProfile {

    public static void run() {
        System.out.println("\n--- Test: Self Profile, State & ToxID Generation ---");
        try {
            // 1. Get initial ToxID
            String initialToxId = MainActivity.get_my_toxid();
            JniToxcoreUnitTest.assertCondition("get_my_toxid returns valid 76-char string", initialToxId != null && initialToxId.length() == 76);
            
            // The first 64 characters are the public key
            String initialPubKey = initialToxId.substring(0, 64);

            // 2. Set and Get Status Message
            String testStatusMsg = "Testing JNI Status 123!";
            MainActivity.tox_self_set_status_message(testStatusMsg);
            JniToxcoreUnitTest.assertCondition("tox_self_set_status_message executes without crash", true);

            String retrievedStatusMsg = MainActivity.tox_self_get_status_message();
            JniToxcoreUnitTest.assertCondition("tox_self_get_status_message matches set value", testStatusMsg.equals(retrievedStatusMsg));

            // 3. Change nospam and verify ToxID changes accordingly
            long newNospam = 0xDEADBEEFL;
            MainActivity.tox_self_set_nospam(newNospam);
            JniToxcoreUnitTest.assertCondition("tox_self_set_nospam executes without crash", true);

            String newToxId = MainActivity.get_my_toxid();
            JniToxcoreUnitTest.assertCondition("get_my_toxid returns valid string after nospam change", newToxId != null && newToxId.length() == 76);
            
            String newPubKey = newToxId.substring(0, 64);
            // Public key must remain identical
            JniToxcoreUnitTest.assertCondition("Public key remains unchanged after nospam change", initialPubKey.equals(newPubKey));
            
            // The last 12 characters (nospam + checksum) must be different
            String initialSuffix = initialToxId.substring(64);
            String newSuffix = newToxId.substring(64);
            JniToxcoreUnitTest.assertCondition("ToxID suffix (nospam+checksum) changes after set_nospam", !initialSuffix.equals(newSuffix));

            // 4. Verify we can read back the exact nospam value we set
            long readNospam = MainActivity.tox_self_get_nospam();
            JniToxcoreUnitTest.assertCondition("tox_self_get_nospam matches set value", readNospam == newNospam);

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("Self Profile test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
