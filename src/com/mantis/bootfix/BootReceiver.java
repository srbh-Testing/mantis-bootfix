package com.mantis.bootfix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    static final String KEY = "bootanim.real_home_ready";
    static boolean toggled = false;

    // First call per process: write 0, then 1 after a short delay so the
    // value really CHANGES and observers fire. Later calls just write 1.
    static synchronized void signal(final Context ctx, final String tag, final Runnable done) {
        final android.content.ContentResolver cr = ctx.getContentResolver();
        try {
            if (!toggled) {
                toggled = true;
                Settings.Secure.putInt(cr, KEY, 0);
                Log.i("MantisBootFix", "uptime=" + android.os.SystemClock.elapsedRealtime() / 1000 + "s set 0 from " + tag);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override public void run() {
                        try {
                            Settings.Secure.putInt(cr, KEY, 1);
                            Log.i("MantisBootFix", "uptime=" + android.os.SystemClock.elapsedRealtime() / 1000 + "s set 1 from " + tag);
                        } catch (Throwable t) {
                            Log.e("MantisBootFix", "set 1 failed", t);
                        }
                        if (done != null) done.run();
                    }
                }, 300);
                return;
            }
            Settings.Secure.putInt(cr, KEY, 1);
            Log.i("MantisBootFix", "uptime=" + android.os.SystemClock.elapsedRealtime() / 1000 + "s set 1 from " + tag);
        } catch (Throwable t) {
            Log.e("MantisBootFix", "signal failed (" + tag + ")", t);
        }
        if (done != null) done.run();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pr = goAsync();
        signal(context.getApplicationContext(), "receiver:" + intent.getAction(), new Runnable() {
            @Override public void run() { pr.finish(); }
        });
    }
}
