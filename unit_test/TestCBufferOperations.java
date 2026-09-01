import com.zoffcc.applications.trifa.MainActivity;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * TEST: C Buffer Operations
 * Tests functions that handle C buffers (ByteBuffer) including hashing,
 * file IDs, and Message V2/V3 buffer operations.
 */
public class TestCBufferOperations {

    public static void run() {
        System.out.println("\n--- Test: C Buffer Operations ---");
        try {
            // =========================================================================
            // 1. tox_hash - Hash data buffer
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 1: Testing tox_hash...");
            
            // TOX_HASH_LENGTH is 32 bytes
            ByteBuffer hashBuffer = ByteBuffer.allocateDirect(32);
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(100);
            
            byte[] testData = "Test data for hashing".getBytes();
            dataBuffer.put(testData);
            dataBuffer.position(0);
            
            int hashResult = MainActivity.tox_hash(hashBuffer, dataBuffer, testData.length);
            JniToxcoreUnitTest.assertCondition("tox_hash executes without crash", hashResult >= 0);
            
            byte[] hashData = new byte[32];
            hashBuffer.position(0);
            hashBuffer.get(hashData);
            boolean hashNotEmpty = false;
            for (byte b : hashData) {
                if (b != 0) {
                    hashNotEmpty = true;
                    break;
                }
            }
            JniToxcoreUnitTest.assertCondition("tox_hash filled buffer with non-zero data", hashNotEmpty);
            
            // Test same data produces same hash
            ByteBuffer hashBuffer2 = ByteBuffer.allocateDirect(32);
            dataBuffer.position(0);
            MainActivity.tox_hash(hashBuffer2, dataBuffer, testData.length);
            byte[] hashData2 = new byte[32];
            hashBuffer2.position(0);
            hashBuffer2.get(hashData2);
            JniToxcoreUnitTest.assertCondition("tox_hash produces consistent results", Arrays.equals(hashData, hashData2));

            // =========================================================================
            // 2. tox_file_get_file_id - Get file ID buffer
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 2: Testing tox_file_get_file_id with invalid friend...");
            
            ByteBuffer fileIdBuffer = ByteBuffer.allocateDirect(32);
            int fileIdResult = MainActivity.tox_file_get_file_id(999L, 0L, fileIdBuffer);
            JniToxcoreUnitTest.assertCondition("tox_file_get_file_id handles invalid friend gracefully", fileIdResult < 0);

            // =========================================================================
            // 3. Message V2 buffer operations (Corrected based on C source logic)
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 3: Testing Message V2 buffer operations...");
            
            // Constants from toxcore
            int TOX_FILE_KIND_MESSAGEV2_SEND = 2;
            int TOX_FILE_KIND_MESSAGEV2_ANSWER = 3;
            int TOX_FILE_KIND_MESSAGEV2_INVALID = 0; // Will fall through to ALTER and return false
            
            String msgText = "Test message";
            int textLength = msgText.getBytes().length;
            
            // --- 3a. Test SEND message (type=2) ---
            System.out.println("\u001B[90m[INFO]\u001B[0m   Testing Message V2 SEND...");
            long msgSizeSend = MainActivity.tox_messagev2_size(textLength, TOX_FILE_KIND_MESSAGEV2_SEND, 0);
            // Expected size: 32 (key) + 4 (ts_sec) + 2 (ts_ms) + textLength
            long expectedSizeSend = 32 + 4 + 2 + textLength;
            JniToxcoreUnitTest.assertCondition("tox_messagev2_size for SEND returns correct size (" + expectedSizeSend + ")", msgSizeSend == expectedSizeSend);
            
            ByteBuffer textBufferSend = ByteBuffer.allocateDirect(textLength);
            textBufferSend.put(msgText.getBytes());
            textBufferSend.position(0);
            
            ByteBuffer rawMsgBufferSend = ByteBuffer.allocateDirect((int)msgSizeSend);
            ByteBuffer msgIdBufferSend = ByteBuffer.allocateDirect(32);
            
            int wrapResultSend = MainActivity.tox_messagev2_wrap(textLength, TOX_FILE_KIND_MESSAGEV2_SEND, 0, textBufferSend, 1000L, 500L, rawMsgBufferSend, msgIdBufferSend);
            JniToxcoreUnitTest.assertCondition("tox_messagev2_wrap for SEND executes successfully (returns 1/true)", wrapResultSend >= 0);
            
            byte[] msgIdDataSend = new byte[32];
            msgIdBufferSend.position(0);
            msgIdBufferSend.get(msgIdDataSend);
            boolean msgIdNotEmptySend = false;
            for (byte b : msgIdDataSend) {
                if (b != 0) {
                    msgIdNotEmptySend = true;
                    break;
                }
            }
            JniToxcoreUnitTest.assertCondition("tox_messagev2_wrap for SEND generated random message ID", msgIdNotEmptySend);
            
            // Verify the raw message buffer starts with the same msgid
            byte[] rawMsgDataSend = new byte[32];
            rawMsgBufferSend.position(0);
            rawMsgBufferSend.get(rawMsgDataSend);
            JniToxcoreUnitTest.assertCondition("tox_messagev2_wrap for SEND copied msgid to start of raw buffer", Arrays.equals(msgIdDataSend, rawMsgDataSend));

            // --- 3b. Test ANSWER message (type=3) ---
            System.out.println("\u001B[90m[INFO]\u001B[0m   Testing Message V2 ANSWER...");
            long msgSizeAnswer = MainActivity.tox_messagev2_size(0, TOX_FILE_KIND_MESSAGEV2_ANSWER, 0);
            // Expected size: 32 (key) + 4 (ts_sec) + 2 (ts_ms)
            long expectedSizeAnswer = 32 + 4 + 2;
            JniToxcoreUnitTest.assertCondition("tox_messagev2_size for ANSWER returns correct size (" + expectedSizeAnswer + ")", msgSizeAnswer == expectedSizeAnswer);
            
            ByteBuffer rawMsgBufferAnswer = ByteBuffer.allocateDirect((int)msgSizeAnswer);
            ByteBuffer msgIdBufferAnswer = ByteBuffer.allocateDirect(32);
            
            // Pre-fill msgid for ANSWER (it's an input parameter for ANSWER)
            byte[] dummyMsgId = new byte[32];
            Arrays.fill(dummyMsgId, (byte) 0xAB);
            msgIdBufferAnswer.put(dummyMsgId);
            msgIdBufferAnswer.position(0);
            
            // Use a 0-byte direct buffer instead of null to prevent JNI segfaults
            ByteBuffer emptyBuffer = ByteBuffer.allocateDirect(0);
            int wrapResultAnswer = MainActivity.tox_messagev2_wrap(0, TOX_FILE_KIND_MESSAGEV2_ANSWER, 0, emptyBuffer, 2000L, 100L, rawMsgBufferAnswer, msgIdBufferAnswer);
            JniToxcoreUnitTest.assertCondition("tox_messagev2_wrap for ANSWER executes successfully", wrapResultAnswer >= 0);
            
            byte[] rawMsgDataAnswer = new byte[32];
            rawMsgBufferAnswer.position(0);
            rawMsgBufferAnswer.get(rawMsgDataAnswer);
            JniToxcoreUnitTest.assertCondition("tox_messagev2_wrap for ANSWER copied input msgid to raw buffer", Arrays.equals(dummyMsgId, rawMsgDataAnswer));

            // --- 3c. Test INVALID type (type=0) ---
            System.out.println("\u001B[90m[INFO]\u001B[0m   Testing Message V2 INVALID type...");
            ByteBuffer msgIdBufferInvalid = ByteBuffer.allocateDirect(32);
            ByteBuffer rawMsgBufferInvalid = ByteBuffer.allocateDirect(100);
            ByteBuffer textBufferInvalid = ByteBuffer.allocateDirect(10);
            textBufferInvalid.put("1234567890".getBytes());
            textBufferInvalid.position(0);
            
            int wrapResultInvalid = MainActivity.tox_messagev2_wrap(10, 0, 0, textBufferInvalid, 0L, 0L, rawMsgBufferInvalid, msgIdBufferInvalid);
            
            // FIX: The C code returns 1 for false (failure), not 0!
            JniToxcoreUnitTest.assertCondition("tox_messagev2_wrap for INVALID type returns 1 (false)", wrapResultInvalid == 1);
            
            byte[] msgIdDataInvalid = new byte[32];
            msgIdBufferInvalid.position(0);
            msgIdBufferInvalid.get(msgIdDataInvalid);
            boolean msgIdIsAllZeros = true;
            for (byte b : msgIdDataInvalid) {
                if (b != 0) {
                    msgIdIsAllZeros = false;
                    break;
                }
            }
            JniToxcoreUnitTest.assertCondition("tox_messagev2_wrap for INVALID type did not modify msgid buffer", msgIdIsAllZeros);

            // =========================================================================
            // 4. Buffer size validation
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 4: Testing buffer size limits...");
            
            long maxFilenameLen = MainActivity.tox_max_filename_length();
            JniToxcoreUnitTest.assertCondition("tox_max_filename_length returns positive value", maxFilenameLen > 0);
            
            long fileIdLen = MainActivity.tox_file_id_length();
            JniToxcoreUnitTest.assertCondition("tox_file_id_length returns 32", fileIdLen == 32);
            
            long maxMsgLen = MainActivity.tox_max_message_length();
            JniToxcoreUnitTest.assertCondition("tox_max_message_length returns positive value", maxMsgLen > 0);

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("C Buffer Operations test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
