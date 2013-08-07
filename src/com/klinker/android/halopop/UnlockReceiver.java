package com.klinker.android.halopop;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UnlockReceiver extends BroadcastReceiver {

    public static boolean openApp = false;
    public static PendingIntent pIntent;

    @Override
    public void onReceive(final Context context, Intent intent) {
        // Receiver will be invoked when device is unlocked and send pending intent to start notification
        if (intent.getAction() != null && openApp == true && pIntent != null) {
            try {
                pIntent.send(context, 0, new Intent().addFlags(0x00002000));
                openApp = false;
                pIntent = null;
            } catch (Exception e) {
                // cancelled...
            }
        }
    }
}