package com.mantis.bootfix;

import android.app.Activity;
import android.os.Bundle;

public class SignalActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        BootReceiver.toggled = false;
        BootReceiver.signal(getApplicationContext(), "activity", new Runnable() {
            @Override public void run() { finish(); }
        });
    }
}
