import com.zoffcc.applications.trifa.TrifaToxService;
import java.nio.charset.StandardCharsets;

/**
 * TEST: TrifaToxService.safe_string
 * Tests the Java-side sanitization method used by the C JNI layer to safely
 * convert raw byte arrays (which may contain invalid UTF-8 or binary data)
 * into Java Strings without causing JVM crashes.
 */
public class TestSafeString {

    public static void run() {
        System.out.println("\n--- Test: TrifaToxService.safe_string ---");
        try {
            // 1. Test null input (simulates C passing NULL)
            String res1 = TrifaToxService.safe_string(null);
            JniToxcoreUnitTest.assertCondition("safe_string(null) returns null", res1 == null);

            // 2. Test empty byte array (simulates C passing len == 0)
            String res2 = TrifaToxService.safe_string(new byte[0]);
            JniToxcoreUnitTest.assertCondition("safe_string(empty array) returns empty string", "".equals(res2));

            // 3. Test valid UTF-8 string (normal Tox name/status message)
            String validStr = "Hello, Tox! Привет мир! 🚀";
            byte[] validBytes = validStr.getBytes(StandardCharsets.UTF_8);
            String res3 = TrifaToxService.safe_string(validBytes);
            JniToxcoreUnitTest.assertCondition("safe_string(valid UTF-8) returns correct string", validStr.equals(res3));

            // 4. Test invalid UTF-8 bytes 
            // 0xC3 0x28 is an invalid UTF-8 sequence (starts a 2-byte char but followed by invalid byte).
            // In C, NewStringUTF would SIGABRT here. In Java, it gracefully replaces with ''.
            byte[] invalidUtf8 = {(byte) 0xC3, (byte) 0x28, (byte) 0x41};
            String res4 = TrifaToxService.safe_string(invalidUtf8);
            JniToxcoreUnitTest.assertCondition("safe_string(invalid UTF-8) does not crash and returns a string", res4 != null);
            JniToxcoreUnitTest.assertCondition("safe_string(invalid UTF-8) has length > 0", res4.length() > 0);
            JniToxcoreUnitTest.assertCondition("safe_string(invalid UTF-8) contains replacement char ''", res4.contains(""));

            // 5. Test binary data containing null bytes 
            // C strings normally terminate at \0, but c_safe_string_from_java passes the explicit length.
            byte[] binaryWithNulls = {0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x00, 0x57, 0x6F, 0x72, 0x6C, 0x64}; // "Hello\0World"
            String res5 = TrifaToxService.safe_string(binaryWithNulls);
            JniToxcoreUnitTest.assertCondition("safe_string(binary with nulls) does not crash", res5 != null);
            JniToxcoreUnitTest.assertCondition("safe_string(binary with nulls) preserves null byte as char", res5.length() == 11);
            JniToxcoreUnitTest.assertCondition("safe_string(binary with nulls) has null char at index 5", res5.charAt(5) == '\0');

            // 6. Test large byte array (simulating large Tox payloads, e.g., large group topics)
            byte[] largeBytes = new byte[100000];
            for (int i = 0; i < largeBytes.length; i++) {
                largeBytes[i] = (byte) (i % 256); // Mix of valid and invalid UTF-8
            }
            String res6 = TrifaToxService.safe_string(largeBytes);
            JniToxcoreUnitTest.assertCondition("safe_string(large byte array) does not crash", res6 != null);
            JniToxcoreUnitTest.assertCondition("safe_string(large byte array) has correct length", res6.length() > 0);

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("Safe String test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
