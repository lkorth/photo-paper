package com.lukekorth.photo_paper.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;

import com.lukekorth.photo_paper.models.User;

import io.realm.Realm;

import static android.content.Context.ACCOUNT_SERVICE;

public class AccountCreator {

    private static final String DEFAULT_ACCOUNT_NAME = "Photo Paper";
    private static final String ACCOUNT_TYPE = "lukekorth.com";
    private static final String AUTHORITY = "com.lukekorth.photo_paper.sync.provider";

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
        Realm realm = Realm.getDefaultInstance();
        User user = User.getUser(realm);
        String username = DEFAULT_ACCOUNT_NAME;
        if (user != null) {
            username = user.getUserName();
        }
        realm.close();

        return new Account(username, ACCOUNT_TYPE);
    }
}
