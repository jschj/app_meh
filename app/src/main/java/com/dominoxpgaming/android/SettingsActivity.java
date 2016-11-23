package com.dominoxpgaming.android;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Window;


/**
 * Created by Jan on 22.10.2016.
 *
 */

public class SettingsActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setTitle("Set");

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_main);

    }
}
