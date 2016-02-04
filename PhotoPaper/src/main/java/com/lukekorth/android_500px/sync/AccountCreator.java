package com.lukekorth.android_500px.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;

import com.lukekorth.android_500px.models.User;

import static android.content.Context.ACCOUNT_SERVICE;

public class AccountCreator {

    private static final String DEFAULT_ACCOUNT_NAME = "Photo Paper";
    private static final String ACCOUNT_TYPE = "lukekorth.com";
    private static final String AUTHORITY = "com.lukekorth.android_500px.sync.provider";

    public static void createAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        Account account = getAccount();
        if (!account.name.equals(DEFAULT_ACCOUNT_NAME)) {
            accountManager.removeAccount(new Account(DEFAULT_ACCOUNT_NAME, ACCOUNT_TYPE), null, null);
        }

        if (((AccountManager) context.getSystemService(ACCOUNT_SERVICE))
                .addAccountExplicitly(account, null, null)) {
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
        }
    }

    public static Account getAccount() {
        User user = User.getUser();
        if (user != null) {
            return new Account(user.userName, ACCOUNT_TYPE);
        } else {
            return new Account(DEFAULT_ACCOUNT_NAME, ACCOUNT_TYPE);
        }
    }
}
