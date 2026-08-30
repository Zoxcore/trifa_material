package com.zoffcc.applications.trifa;

public class TrifaToxService {
    // Called by C code for logging
    public static void logger(int level, String message) {
        // Optional: uncomment to see Tox core logs during tests
        // System.out.println("[TOX LOG " + level + "] " + message);
    }

    // Called by C code to safely convert byte arrays to Java Strings
    public static String safe_string(byte[] data) {
        if (data == null) return null;
        return new String(data);
    }
}
