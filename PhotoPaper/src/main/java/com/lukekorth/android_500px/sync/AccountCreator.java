package com.lukekorth.android_500px.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;

import static android.content.Context.ACCOUNT_SERVICE;

public class AccountCreator {

    private static final String ACCOUNT_TYPE = "lukekorth.com";
    private static final String AUTHORITY = "com.lukekorth.android_500px.sync.provider";

    public static void createAccount(Context context) {
        Account account = getAccount();
        if (((AccountManager) context.getSystemService(ACCOUNT_SERVICE))
                .addAccountExplicitly(account, null, null)) {
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
        }
    }

    public static Account getAccount() {
        return new Account("fakeaccount", ACCOUNT_TYPE);
    }
}
