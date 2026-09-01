import com.zoffcc.applications.trifa.MainActivity;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TEST: Text Messaging
 * Tests text message sending operations including edge cases,
 * special characters, and concurrent messaging.
 */
public class TestTextMessaging {

    public static void run() {
        System.out.println("\n--- Test: Text Messaging ---");
        try {
            // =========================================================================
            // 1. Basic message sending to non-existent friend
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 1: Testing message sending to non-existent friend...");
            
            long sendResult = MainActivity.tox_friend_send_message(999L, 0, "Hello");
            JniToxcoreUnitTest.assertCondition("tox_friend_send_message handles non-existent friend gracefully", sendResult < 0);

            // =========================================================================
            // 2. Message with special characters
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 2: Testing messages with special characters...");
            
            String[] testMessages = {
                "Simple ASCII message",
                "Message with emojis: 🚀🎉👍",
                "Unicode: Привет мир! 你好世界",
                "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?",
                "Newlines:\nLine 1\nLine 2\nLine 3",
                "Tabs:\tTab1\t\tTab2",
                "Mixed: Hello 🌍 World 世界 🎊"
            };
            
            for (int i = 0; i < testMessages.length; i++) {
                long result = MainActivity.tox_friend_send_message(999L, 0, testMessages[i]);
                JniToxcoreUnitTest.assertCondition("Message with special chars handled (msg " + (i+1) + ")", result < 0);
            }

            // =========================================================================
            // 3. Empty and very long messages
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 3: Testing edge case message lengths...");
            
            // Empty message
            long emptyResult = MainActivity.tox_friend_send_message(999L, 0, "");
            JniToxcoreUnitTest.assertCondition("Empty message handled gracefully", emptyResult < 0);
            
            // Very long message (should exceed max length)
            StringBuilder longMsg = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                longMsg.append("A");
            }
            long longResult = MainActivity.tox_friend_send_message(999L, 0, longMsg.toString());
            JniToxcoreUnitTest.assertCondition("Very long message handled gracefully", longResult < 0);

            // =========================================================================
            // 4. Message V3 sending
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 4: Testing Message V3 sending...");
            
            java.nio.ByteBuffer msgHash = java.nio.ByteBuffer.allocateDirect(32);
            MainActivity.tox_messagev3_get_new_message_id(msgHash);
            
            long v3Result = MainActivity.tox_messagev3_friend_send_message(999L, 0, "V3 message", msgHash, System.currentTimeMillis());
            JniToxcoreUnitTest.assertCondition("tox_messagev3_friend_send_message handles non-existent friend", v3Result < 0);

            // =========================================================================
            // 5. Group messaging
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 5: Testing group messaging...");
            
            long groupMsgResult = MainActivity.tox_group_send_message(999L, 0, "Group message");
            JniToxcoreUnitTest.assertCondition("tox_group_send_message handles non-existent group", groupMsgResult < 0);
            
            long groupPrivateResult = MainActivity.tox_group_send_private_message(999L, 0L, 0, "Private group message");
            JniToxcoreUnitTest.assertCondition("tox_group_send_private_message handles non-existent group", groupPrivateResult < 0);

            // =========================================================================
            // 6. Conference messaging
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 6: Testing conference messaging...");
            
            int confMsgResult = MainActivity.tox_conference_send_message(999L, 0, "Conference message");
            JniToxcoreUnitTest.assertCondition("tox_conference_send_message handles non-existent conference", confMsgResult < 0);

            // =========================================================================
            // 7. Concurrent message sending stress test
            // =========================================================================
            System.out.println("\u001B[90m[INFO]\u001B[0m Phase 7: Concurrent message sending stress test...");
            
            int numThreads = 10;
            int messagesPerThread = 50;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(numThreads);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < messagesPerThread; j++) {
                            String msg = "Thread " + threadId + " message " + j;
                            MainActivity.tox_friend_send_message(999L, 0, msg);
                        }
                    } catch (Throwable t) {
                        errorCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                }).start();
            }
            
            startLatch.countDown();
            boolean finished = doneLatch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            
            JniToxcoreUnitTest.assertCondition("Concurrent messaging finished within timeout", finished);
            JniToxcoreUnitTest.assertCondition("Concurrent messaging had zero errors", errorCount.get() == 0);

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("Text Messaging test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}

