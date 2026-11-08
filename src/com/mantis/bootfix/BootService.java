package com.mantis.bootfix;

import android.accessibilityservice.AccessibilityService;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootService extends AccessibilityService {
    private boolean done = false;

    private void signalReady(final String tag) {
        try {
            Settings.Secure.putInt(getContentResolver(), "bootanim.real_home_ready", 1);
            Log.i("MantisBootFix", "Sent bootanim.real_home_ready=1 from " + tag);
        } catch (Throwable t) {
            Log.e("MantisBootFix", "Failed to set real_home_ready", t);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        signalReady("onCreate");
        final Handler h = new Handler(Looper.getMainLooper());
        // Fire every 500ms for 10 seconds during early boot to guarantee FireTvWmsCallback catches it
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
