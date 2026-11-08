package com.mantis.bootfix;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootService extends AccessibilityService {
    private boolean done = false;

    private void signalReady(final String tag) {
        BootReceiver.signal(getApplicationContext(), tag, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        signalReady("onCreate");
        final Handler h = new Handler(Looper.getMainLooper());
        for (int i = 1; i <= 20; i++) {
            final int step = i;
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    signalReady("timer_" + step);
                }
            }, i * 500L);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        signalReady("onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!done) {
            done = true;
            signalReady("firstEvent");
        }
    }

    @Override
    public void onInterrupt() {}
}
