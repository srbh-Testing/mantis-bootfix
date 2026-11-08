package com.mantis.bootfix;

import android.accessibilityservice.AccessibilityService;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class BootService extends AccessibilityService {
    private String launcher;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        launcher = BootReceiver.launcherPkg(getApplicationContext());
        Log.i("MantisBootFix", BootReceiver.up() + "service connected, launcher=" + launcher);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (BootReceiver.signaled || e == null || e.getPackageName() == null) return;
        if (SystemClock.elapsedRealtime() > 120000) return;
        if (launcher == null) launcher = BootReceiver.launcherPkg(getApplicationContext());
        if (launcher.equals(e.getPackageName().toString())) {
            BootReceiver.signalOnce(getApplicationContext(), "launcher-window", null);
        }
    }

    @Override
    public void onInterrupt() {}
}
