import com.zoffcc.applications.trifa.MainActivity;

/**
 * TEST: Bootstrap
 * Tests bootstrapping from multiple UDP and TCP nodes and waits for Tox to come online.
 * Node data sourced from https://nodes.tox.chat/json (filtered for active status_udp/status_tcp).
 */
public class TestBootstrap {

    // Well-known Tox bootstrap nodes (UDP)
    private static final String[][] UDP_NODES = {
        {"144.217.167.73", "33445", "7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C"},
        {"tox1.mf-net.eu", "33445", "B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506"},
        {"139.162.110.188", "33445", "F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55"},
        {"tox2.mf-net.eu", "33445", "70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F"},
        {"tox.initramfs.io", "33445", "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25"}
    };

    // Well-known Tox bootstrap nodes (TCP)
    private static final String[][] TCP_NODES = {
        {"144.217.167.73", "33445", "7E5668E0EE09E19F320AD47902419331FFEE147BB3606769CFBE921A2A2FD34C"},
        {"tox1.mf-net.eu", "33445", "B3E5FA80DC8EBD1149AD2AB35ED8B85BD546DEDE261CA593234C619249419506"},
        {"139.162.110.188", "443",   "F76A11284547163889DDC89A7738CF271797BF5E5E220643E97AD3C7E7903D55"},
        {"tox2.mf-net.eu", "33445", "70EA214FDE161E7432530605213F18F7427DC773E276B3E317A07531F548545F"},
        {"tox.initramfs.io", "3389", "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25"}
    };

    // Connection status constants (from tox.h)
    private static final int TOX_CONNECTION_NONE = 0;
    private static final int TOX_CONNECTION_TCP = 1;
    private static final int TOX_CONNECTION_UDP = 2;

    // Timeout for waiting to come online (in milliseconds)
    private static final long ONLINE_TIMEOUT_MS = 60000; // 1 minute
    private static final long POLL_INTERVAL_MS = 200;    // Check every 200ms

    public static void run() {
        System.out.println("\n--- Test: Bootstrap ---");
        try {
            // 1. Bootstrap from multiple UDP nodes
            System.out.println("\u001B[90m[INFO]\u001B[0m Bootstrapping from UDP nodes...");
            for (String[] node : UDP_NODES) {
                String ip = node[0];
                long port = Long.parseLong(node[1]);
                String key = node[2];
                
                int result = MainActivity.bootstrap_single(ip, key, port);
                if (result == 0) {
                    System.out.println("\u001B[90m[INFO]\u001B[0m   -> UDP bootstrap success: " + ip + ":" + port);
                } else {
                    System.out.println("\u001B[90m[INFO]\u001B[0m   -> UDP bootstrap failed (code " + result + "): " + ip + ":" + port);
                }
            }
            JniToxcoreUnitTest.assertCondition("UDP bootstrap calls executed without crash", true);

            // 2. Add multiple TCP relays
            System.out.println("\u001B[90m[INFO]\u001B[0m Adding TCP relays...");
            for (String[] node : TCP_NODES) {
                String ip = node[0];
                long port = Long.parseLong(node[1]);
                String key = node[2];
                
                int result = MainActivity.add_tcp_relay_single(ip, key, port);
                if (result == 0) {
                    System.out.println("\u001B[90m[INFO]\u001B[0m   -> TCP relay added successfully: " + ip + ":" + port);
                } else {
                    System.out.println("\u001B[90m[INFO]\u001B[0m   -> TCP relay add failed (code " + result + "): " + ip + ":" + port);
                }
            }
            JniToxcoreUnitTest.assertCondition("TCP relay calls executed without crash", true);

            // 3. Wait for Tox to come online
            System.out.println("\u001B[90m[INFO]\u001B[0m Bootstrap initiated. Waiting for Tox to come online...");
            
            long startTime = System.currentTimeMillis();
            int connectionStatus = TOX_CONNECTION_NONE;
            boolean cameOnline = false;

            while (System.currentTimeMillis() - startTime < ONLINE_TIMEOUT_MS) {
                connectionStatus = MainActivity.tox_self_get_connection_status();

                if (connectionStatus != TOX_CONNECTION_NONE) {
                    cameOnline = true;
                    break;
                }

                Thread.sleep(POLL_INTERVAL_MS);
            }

            long elapsed = System.currentTimeMillis() - startTime;

            // 4. Evaluate results
            if (cameOnline) {
                String connectionType = (connectionStatus == TOX_CONNECTION_TCP) ? "TCP" : "UDP";
                System.out.println("\u001B[90m[INFO]\u001B[0m Tox came online via " + connectionType + " after " + elapsed + "ms");
                JniToxcoreUnitTest.assertCondition("Tox came online within timeout", true);
                JniToxcoreUnitTest.assertCondition("Connection status is valid (" + connectionType + ")", connectionStatus > 0);
            } else {
                System.out.println("\u001B[90m[INFO]\u001B[0m Tox did not come online within " + ONLINE_TIMEOUT_MS + "ms timeout");
                JniToxcoreUnitTest.assertCondition("Tox came online within timeout", false);
            }

        } catch (UnsatisfiedLinkError e) {
            JniToxcoreUnitTest.assertCondition("Bootstrap test failed: UnsatisfiedLinkError", false);
            System.err.println("Native library not found. Ensure -Djava.library.path is set correctly.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            JniToxcoreUnitTest.assertCondition("Bootstrap test failed: Thread interrupted", false);
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (Exception e) {
            JniToxcoreUnitTest.assertCondition("Bootstrap test failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
}
