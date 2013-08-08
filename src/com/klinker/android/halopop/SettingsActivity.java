package com.klinker.android.halopop;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by luke on 8/8/13.
 */
public class SettingsActivity extends PreferenceActivity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }

}
