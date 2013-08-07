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

// HUGE thanks to the PA team, this is all based on their amazing work!

package com.klinker.android.halopop;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import com.klinker.android.halopop.ApplicationsDialog.AppAdapter;
import com.klinker.android.halopop.ApplicationsDialog.AppItem;

import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends PreferenceActivity {

    private static final int MENU_ADD = 0;

    private Context mContext;

    // Preference screen to hold all items we have added to the list
    private PreferenceScreen mRoot;

    // information on installed apps and related preferences
    private List<ResolveInfo> mInstalledApps;
    private AppAdapter mAppAdapter;
    private Preference mPreference;

    // called to remove item from list when clicked
    private Preference.OnPreferenceClickListener mOnItemClickListener = new Preference.OnPreferenceClickListener(){
        @Override
        public boolean onPreferenceClick(Preference arg0) {
            mRoot.removePreference(arg0);
            savePreferenceItems();
            invalidateOptionsMenu();
            return false;
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        // set up intent used to search for all apps that show in the launcher
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mInstalledApps = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);

        // creates applications dialog that lists all installed apps
        ApplicationsDialog appDialog = new ApplicationsDialog();
        mAppAdapter = appDialog.createAppAdapter(mContext, mInstalledApps);
        mAppAdapter.update();

        // set up screen with previously added apps
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));
        mRoot = getPreferenceScreen();
        loadPreferenceItems();

        // check if PA is on device
        findPA();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // create a menu item to add a new application to the list and add that item to the menu
        menu.add(Menu.NONE, MENU_ADD, 0, R.string.add)
            .setIcon(R.drawable.ic_add)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD: // add new app to list

                // Create listview with all launcher apps listed
                final ListView list = new ListView(mContext);
                list.setAdapter(mAppAdapter);

                // create new dialog box with applications list
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.choose_app);
                builder.setView(list);
                final Dialog dialog = builder.create();

                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {

                        // if clicked item already on the list, do nothing and return
                        final AppItem info = (AppItem) arg0.getItemAtPosition(arg2);
                        final String packageName = info.packageName;

                        for (int i = 0; i < mRoot.getPreferenceCount(); i++) {
                            if (mRoot.getPreference(i).getSummary().equals(packageName)) {
                                return;
                            }
                        }

                        // create new preference, add it to the list, destroy dialog, and save
                        // updated preferences
                        mPreference = new Preference(mContext);
                        mPreference.setOnPreferenceClickListener(mOnItemClickListener);
                        mPreference.setTitle(info.title);
                        mPreference.setSummary(packageName);
                        mPreference.setIcon(info.icon);
                        mRoot.addPreference(mPreference);
                        invalidateOptionsMenu();
                        dialog.cancel();
                        savePreferenceItems();
                    }
                });

                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void savePreferenceItems() {
        ArrayList<String> items = new ArrayList<String>();

        // add package name of current items in the preference list to the arraylist
        for (int i = 0; i < mRoot.getPreferenceCount(); i++){
            String packageName = mRoot.getPreference(i)
                    .getSummary().toString();
            items.add(packageName);
        }

        // save the arraylist to shared prefs
        Utils.saveArray(items.toArray(new String[items.size()]), mContext);
    }
    
    public void loadPreferenceItems() {
        // load all packages that have been added to list
        String[] packages = Utils.loadArray(mContext);

        if (packages == null) return;

        for(String packageName : packages){
            // create new preference and add it to the list
            Preference app = new Preference(mContext);
            app.setTitle(Utils.getApplicationName(packageName, mContext));
            app.setSummary(packageName);
            app.setIcon(Utils.getApplicationIconDrawable(packageName, mContext));
            app.setOnPreferenceClickListener(mOnItemClickListener);
            mRoot.addPreference(app);
        }
    }

    public void findPA() {
        // determine if this is the first run of app or not
        boolean firstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);

        if (firstRun) {
            // check if user is running pa
            String hasPa = Utils.getProp("ro.pa");

            if (!hasPa.equals("true")) {
                // if not, show dialog telling them to get it, because it is hands down the best :)
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(R.string.nopa_content)
                        .setTitle(R.string.nopa_title)
                        .setPositiveButton(R.string.nopa_download, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Take user to download page
                                String url = "http://download.paranoidandroid.co/roms/";
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                            }
                        })
                        .setNegativeButton(R.string.nopa_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                                // Save that first run is over
                                getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("firstrun", false)
                                        .commit();

                                checkNotificationListenerEnabled();
                            }
                        });

                AlertDialog nopa_dialog = builder.create();
                nopa_dialog.show();
            }
        }
    }

    public void checkNotificationListenerEnabled() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(R.string.notification_listener_content)
                .setTitle(R.string.notification_listener_title)
                .setPositiveButton(R.string.notification_listener_continue, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Take user to notification listener settings page
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
                });

        AlertDialog notificaionSettings = builder.create();
        notificaionSettings.show();
    }
}