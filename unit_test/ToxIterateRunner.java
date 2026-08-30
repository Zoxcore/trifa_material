import com.zoffcc.applications.trifa.MainActivity;

/**
 * Manages a background thread to continuously call tox_iterate().
 * This is required for the Tox core to process network events, maintain connections, and handle internal state.
 */
public class ToxIterateRunner {
    private static volatile boolean running = false;
    private static Thread iterateThread = null;

    public static void start() {
        if (running) {
            return;
        }
        running = true;
        iterateThread = new Thread(() -> {
            while (running) {
                try {
                    // Sleep for 100ms as requested
                    Thread.sleep(100);
                    
                    // Call the static native method to process Tox events
                    MainActivity.tox_iterate();
                } catch (InterruptedException e) {
                    // Thread was interrupted, time to stop cleanly
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable t) {
                    // Catch any JNI errors or exceptions to prevent the thread from silently dying
                    System.err.println("[WARN] Exception in Tox iterate thread: " + t.getMessage());
                    t.printStackTrace();
                }
            }
        }, "Tox-Iterate-Thread");
        
        // Set as daemon so it doesn't prevent JVM shutdown if something goes wrong
        iterateThread.setDaemon(true);
        iterateThread.start();
        System.out.println("\u001B[90m[INFO]\u001B[0m Tox iterate background thread started (100ms interval).");
    }

    public static void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (iterateThread != null) {
            iterateThread.interrupt();
            try {
                // Wait for the thread to finish, with a 1-second timeout
                iterateThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            iterateThread = null;
        }
        System.out.println("\u001B[90m[INFO]\u001B[0m Tox iterate background thread stopped.");
    }
}
