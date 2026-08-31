import com.zoffcc.applications.trifa.MainActivity;

/**
 * TEST: Friend Management Edge Cases
 * Tests boundary conditions for friend management, ensuring invalid operations 
 * fail gracefully without corrupting state or crashing.
 */
public class TestFriendEdgeCases {

    public static void run() {
        System.out.println("\n--- Test: Friend Management Edge Cases ---");
        try {
            // 1. Initial friend list size should be 0
            long initialSize = MainActivity.tox_self_get_friend_list_size();
            JniToxcoreUnitTest.assertCondition("Initial friend list size is 0", initialSize == 0);

            // 2. Get own ToxID (76 chars)
            String myToxId = MainActivity.get_my_toxid();
            JniToxcoreUnitTest.assertCondition("get_my_toxid returns 76 chars", myToxId != null && myToxId.length() == 76);

            // 3. Try to add ourselves as a friend using our own ToxID.
            // Tox core explicitly prevents adding yourself and should return an error code.
            long resAddSelf = MainActivity.tox_friend_add(myToxId, "Adding myself");
            JniToxcoreUnitTest.assertCondition("tox_friend_add rejects adding self (returns < 0)", resAddSelf < 0);

            // 4. Try to delete a non-existent friend (e.g., friend number 9999).
            // The C code returns boolean: 0 (false) on failure, 1 (true) on success.
            // It should return 0 (false) instead of crashing.
            int resDelete = MainActivity.tox_friend_delete(9999L);
            JniToxcoreUnitTest.assertCondition("tox_friend_delete handles non-existent friend without crashing (returns 0/false)", resDelete == 0);

            // 5. Verify friend list size is still 0 after the failed operations
            long finalSize = MainActivity.tox_self_get_friend_list_size();
            JniToxcoreUnitTest.assertCondition("Friend list size remains 0 after failed operations", finalSize == 0);

        } catch (Throwable e) {
            JniToxcoreUnitTest.assertCondition("Friend Edge Cases test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
