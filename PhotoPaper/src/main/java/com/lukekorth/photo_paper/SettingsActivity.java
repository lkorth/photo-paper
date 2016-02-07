package com.lukekorth.photo_paper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lukekorth.photo_paper.sync.AccountCreator;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        AccountCreator.createAccount(this);
    }
}
