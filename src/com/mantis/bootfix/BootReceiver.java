package com.mantis.bootfix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Settings.Secure.putInt(context.getContentResolver(), "bootanim.real_home_ready", 1);
            Log.i("MantisBootFix", "Receiver set bootanim.real_home_ready=1 on " + intent.getAction());
        } catch (Throwable t) {
            Log.e("MantisBootFix", "Receiver error", t);
        }
    }
}
