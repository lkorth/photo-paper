package com.lukekorth.photo_paper;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.lukekorth.photo_paper.sync.AccountCreator;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        AccountCreator.createAccount(this);
    }
}
