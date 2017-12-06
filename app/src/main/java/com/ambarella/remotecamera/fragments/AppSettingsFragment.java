package com.ambarella.remotecamera.fragments;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.EditText;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.ambarella.remotecamera.R;

/**
 * Created by test on 4/26/16.
 */
public class AppSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
