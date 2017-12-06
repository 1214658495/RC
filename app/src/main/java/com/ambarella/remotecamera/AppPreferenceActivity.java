package com.ambarella.remotecamera;

import android.app.Activity;
import android.os.Bundle;

import com.ambarella.remotecamera.fragments.AppSettingsFragment;

/**
 * Created by test on 5/5/16.
 * Activity to load the AppSettingsFragment
 */
public class AppPreferenceActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new AppSettingsFragment()).commit();
    }
}
