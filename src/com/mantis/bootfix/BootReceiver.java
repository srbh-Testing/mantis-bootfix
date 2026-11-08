package com.mantis.bootfix;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    static final String KEY = "bootanim.real_home_ready";
    static final String ACT_FALLBACK = "com.mantis.bootfix.FALLBACK";
    static final long FALLBACK_MS = 5000;
    static boolean toggled = false;
    static boolean signaled = false;
    static boolean armed = false;

    static String up() {
        return "uptime=" + SystemClock.elapsedRealtime() / 1000 + "s ";
    }

    static String launcherPkg(Context c) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            ResolveInfo ri = c.getPackageManager().resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY);
            if (ri != null && ri.activityInfo != null) {
                String p = ri.activityInfo.packageName;
                if (p != null && !"android".equals(p)) return p;
            }
        } catch (Throwable t) { }
        return "com.spocky.projengmenu";
    }

    static PendingIntent fallbackPi(Context c) {
        Intent i = new Intent(c, BootReceiver.class).setAction(ACT_FALLBACK);
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Write 0, then 1 after a short delay so the value really changes.
    static synchronized void signal(final Context ctx, final String tag, final Runnable done) {
        final ContentResolver cr = ctx.getContentResolver();
        try {
            if (!toggled) {
                toggled = true;
                Settings.Secure.putInt(cr, KEY, 0);
                Log.i("MantisBootFix", up() + "set 0 from " + tag);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override public void run() {
                        try {
                            Settings.Secure.putInt(cr, KEY, 1);
                            Log.i("MantisBootFix", up() + "set 1 from " + tag);
                        } catch (Throwable t) {
                            Log.e("MantisBootFix", "set 1 failed", t);
                        }
                        if (done != null) done.run();
                    }
                }, 300);
                return;
            }
            Settings.Secure.putInt(cr, KEY, 1);
            Log.i("MantisBootFix", up() + "set 1 from " + tag);
        } catch (Throwable t) {
            Log.e("MantisBootFix", "signal failed (" + tag + ")", t);
        }
        if (done != null) done.run();
    }

    // Only once per boot; also cancels the fallback alarm.
    static synchronized void signalOnce(Context ctx, String tag, Runnable done) {
        if (signaled) {
            if (done != null) done.run();
            return;
        }
        signaled = true;
        try {
            ((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE)).cancel(fallbackPi(ctx));
        } catch (Throwable t) { }
        signal(ctx, tag, done);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context app = context.getApplicationContext();
        String a = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(a)
                || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(a)) {
            if (signaled || armed) return;
            armed = true;
            try {
                ((AlarmManager) app.getSystemService(Context.ALARM_SERVICE)).set(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + FALLBACK_MS,
                        fallbackPi(app));
            } catch (Throwable t) {
                Log.e("MantisBootFix", "alarm failed", t);
            }
            Log.i("MantisBootFix", up() + "boot received (" + a + "), waiting for launcher " + launcherPkg(app));
            return;
        }

        final PendingResult pr = goAsync();
        Runnable fin = new Runnable() {
            @Override public void run() { pr.finish(); }
        };
        if (ACT_FALLBACK.equals(a)) {
            signalOnce(app, "fallback", fin);
        } else {
            signal(app, "receiver:" + a, fin);
        }
    }
}
