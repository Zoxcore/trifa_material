package com.zoffcc.applications.trifa;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class CustomSemaphore extends Semaphore {

    private final static String TAG = "trifa.CstSemphr";

    private final boolean CUSTOM_SEMAPHORE_LOGGING = false;
    private boolean acquired = false;
    private String prev_acquired_sourcefile_line = "";
    private final int BLOCKING_THRESHOLD_MS = 500;
    private final int WAIT_FOR_UNBLOCKING_MS = 1000;
    private final long SEM_ID = new Random().nextLong();

    public CustomSemaphore(int permits) {
        super(permits);
        if (CUSTOM_SEMAPHORE_LOGGING) Log.i(TAG, ""+SEM_ID + " " + "create");
    }

    @Override
    public void acquire() throws InterruptedException {
        acquire(null);
    }

    public void acquire_passthru() throws InterruptedException {
        super.acquire();
    }

    public void acquire(String sourcefile_line_) throws InterruptedException {

        String callerMethodName = "";

        if ((sourcefile_line_ != null) && (!sourcefile_line_.isEmpty()))
        {
            callerMethodName = " called from:" + sourcefile_line_;
        }
        else {
            try {
                final StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
                // HINT: from kotlin this is just useless for now :-(
                StackTraceElement element = stacktrace[2];
                callerMethodName = " called from:" + element.getMethodName();
            } catch (Exception ignored) {
            }
        }

        if (CUSTOM_SEMAPHORE_LOGGING) Log.i(TAG, ""+SEM_ID + " " + "acquire:start" + callerMethodName);
        acquired = false;
        final String callerMethodName_final = callerMethodName;
        try {
            final Thread _t = new Thread(() -> {
                try {
                    Thread.sleep(BLOCKING_THRESHOLD_MS);
                    if (!acquired)
                    {
                        // Log.i(TAG,""+SEM_ID + " " + "************* SEM:BLOCKING *************" + callerMethodName_final + " prev: " + prev_acquired_sourcefile_line);
                        /*
                        while (acquired) {
                            Thread.sleep(WAIT_FOR_UNBLOCKING_MS);
                            Log.i(TAG,""+SEM_ID + " " + "!!!!!!!!!!!!! SEM:BLOCKING !!!!!!!!!!!!!" + callerMethodName_final + " prev: " + prev_acquired_sourcefile_line);
                        }
                        Log.i(TAG,""+SEM_ID + " " + "############# SEM:UN-BLOCKING ##########" + callerMethodName_final + " prev: " + prev_acquired_sourcefile_line);
                         */
                    }
                } catch (Exception e) {
                    if (CUSTOM_SEMAPHORE_LOGGING) e.printStackTrace();
                }
            });
            _t.start();
        } catch (Exception e) {
            if (CUSTOM_SEMAPHORE_LOGGING) e.printStackTrace();
        }
        super.acquire();
        prev_acquired_sourcefile_line = sourcefile_line_;
        acquired = true;
        if (CUSTOM_SEMAPHORE_LOGGING) Log.i(TAG, ""+SEM_ID + " " + "acquire:finish" + callerMethodName);
    }

    public void release_passthru()
    {
        super.release();
    }

    @Override
    public void release() {
        if (CUSTOM_SEMAPHORE_LOGGING) Log.i(TAG, ""+SEM_ID + " " + "release:start");
        super.release();
        if (CUSTOM_SEMAPHORE_LOGGING) Log.i(TAG, ""+SEM_ID + " " + "release:finish");
    }
}
