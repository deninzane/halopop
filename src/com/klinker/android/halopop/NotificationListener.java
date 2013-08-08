/*
 * Copyright 2013 Jacob Klinker
 * This code has been modified. Portions copyright (C) 2012, ParanoidAndroid Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.halopop;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // get the pending intent of the notification and package name
        PendingIntent pIntent = sbn.getNotification().contentIntent;
        String creatorPackage = pIntent.getCreatorPackage();

        // get apps that are to be used by halo pop
        String[] activeApps = Utils.loadArray(this);

        // check if this should run or not under the lockscreen
        if(pm.isScreenOn() || (!pm.isScreenOn() && sharedPrefs.getBoolean("unlock_settings", true)))
        {
            for (int i = 0; i < activeApps.length; i++) {
                if (activeApps[i].equals(creatorPackage)) {
                    pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

                    if (pm.isScreenOn()) {
                        // if app is to be used, then apply the pending intent with added flag for multiwindow
                        try {
                            pIntent.send(this, 0, new Intent().addFlags(Utils.FLAG_PA_MULTIWINDOW));
                        } catch (Exception e) {
                            // pending intent was cancelled...
                        }
                    } else {
                        // Store pending intent to be activated when screen unlocked
                        UnlockReceiver.openApp = true;
                        UnlockReceiver.pIntent = pIntent;
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Do nothing, we don't care when notifications are removed
    }
}
