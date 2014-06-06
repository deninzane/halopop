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
        if (intent.getAction() != null && openApp == true && pIntent != null && !Utils.checkRunning(pIntent.getCreatorPackage(), context)) {
            try {
                pIntent.send(context, 0, new Intent().addFlags(Utils.FLAG_PA_MULTIWINDOW));
                openApp = false;
                pIntent = null;
            } catch (Exception e) {
                // cancelled...
            }
        }
    }
}
