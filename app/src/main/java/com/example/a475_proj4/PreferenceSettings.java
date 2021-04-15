package com.example.a475_proj4;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferenceSettings extends PreferenceActivity{

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);

    }
}