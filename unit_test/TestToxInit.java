import com.zoffcc.applications.trifa.MainActivity;
import java.io.File;
import java.nio.file.Files;

/**
 * TEST: Tox Initialization
 * Tests the JNI init method to ensure it properly initializes the tox_global instance.
 */
public class TestToxInit {

    public static void run() {
        System.out.println("\n--- Test: Tox Initialization ---");
        try {
            File tempDir = Files.createTempDirectory("trifa_jni_test_").toFile();
            tempDir.deleteOnExit();
            String dataDir = tempDir.getAbsolutePath();

            String passphraseHash = "!00100000002200000000000000003300000000000000000aa000000xff0000$";

            MainActivity tox = new MainActivity();
            System.out.println("\u001B[90m[INFO]\u001B[0m Calling tox init...");
            tox.init(dataDir, 1, 1, 0, "127.0.0.1", 9050L,
                     passphraseHash, 1, 0, 2500, 30, 64, 48000, 2);
            JniToxcoreUnitTest.assertCondition("tox init executes without crash", true);

            // Set a test name to verify write/read bindings
            tox.tox_self_set_name("JniTestUser");
            JniToxcoreUnitTest.assertCondition("tox_self_set_name executes without crash", true);

            long nameSize = tox.tox_self_get_name_size();
            JniToxcoreUnitTest.assertCondition("tox_self_get_name_size > 0 after setting name", nameSize > 0);

            String name = tox.tox_self_get_name();
            JniToxcoreUnitTest.assertCondition("tox_self_get_name returns non-null after init", name != null);
            JniToxcoreUnitTest.assertCondition("tox_self_get_name matches what we set", "JniTestUser".equals(name));

            deleteDirectory(tempDir);
        } catch (UnsatisfiedLinkError e) {
            JniToxcoreUnitTest.assertCondition("init UnsatisfiedLinkError: " + e.getMessage(), false);
            e.printStackTrace();
        } catch (Exception e) {
            JniToxcoreUnitTest.assertCondition("init Exception: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private static void deleteDirectory(File dir) {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) deleteDirectory(f);
        }
        dir.delete();
    }
}
