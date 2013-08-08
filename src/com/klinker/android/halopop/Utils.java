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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static final String ITEMS = "items";
    
    private static final int FLAG_PA_MULTIWINDOW = 0x00002000;

    // tell if package is installed on device or not
    public static boolean packageExists(Context context, String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage))
                return true;
        }

        return false;
    }

    // find drawable for icon based on package name
    public static Drawable getApplicationIconDrawable(String packageName, Context context){
        Drawable appIcon = null;

        try {
            appIcon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {

        }

        return appIcon;
    }

    // get the application name based on package name
    public static String getApplicationName(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;

        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final NameNotFoundException e) {
            ai = null;
        }

        final String applicationName = (String) (ai != null ?
                pm.getApplicationLabel(ai) : context.getString(R.string.unknown));

        return applicationName;
    }

    // save the array of added apps to the shared prefs
    public static boolean saveArray(String[] array, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putInt(ITEMS +"_size", array.length);

        for (int i = 0; i < array.length; i++) {
            editor.putString(ITEMS + "_" + i, array[i]);
        }

        return editor.commit();
    }

    // load the array of added apps back in from shared prefs
    public static String[] loadArray(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        int size = prefs.getInt(ITEMS + "_size", 0);
        ArrayList<String> packages = new ArrayList<String>();

        if (size != 0) {
            for (int i = 0; i < size; i++) {
                String packageName = prefs.getString(ITEMS + "_" + i, null);

                if (packageExists(context, packageName)) {
                    packages.add(packageName);
                }
            }
        }

        String[] array = packages.toArray(new String[packages.size()]);
        return array;
    }

    // check if paranoid android is the current rom a user is running
    public static String getProp(String prop) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + prop);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            return log.toString();
        } catch (IOException e) {
            // Runtime error
        }
        return null;
    }
}
