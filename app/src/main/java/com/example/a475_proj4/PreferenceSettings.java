package com.example.a475_proj4;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;



import androidx.appcompat.app.AppCompatActivity;

public class PreferenceSettings extends PreferenceActivity {

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);

    }

}