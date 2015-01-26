package com.lukekorth.android_500px;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import fr.nicolaspomepuy.discreetapprate.AppRate;
import fr.nicolaspomepuy.discreetapprate.RetryPolicy;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        AppRate.with(this)
                .text(R.string.rate_app)
                .initialLaunchCount(3)
                .retryPolicy(RetryPolicy.EXPONENTIAL)
                .checkAndShow();
    }
}
