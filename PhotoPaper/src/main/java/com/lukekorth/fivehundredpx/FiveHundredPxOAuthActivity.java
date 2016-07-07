package com.lukekorth.fivehundredpx;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.lukekorth.photo_paper.BuildConfig;
import com.lukekorth.photo_paper.R;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class FiveHundredPxOAuthActivity extends Activity {

    public static final int FAILED = 500;
    public static final String ACCESS_TOKEN = "com.lukekorth.fivehundredpx.fivehundredpxoauthactivity.ACCESS_TOKEN";
    public static final String EXCEPTION = "com.lukekorth.fivehundredpx.fivehundredpxoauthactivity.EXCEPTION";

    private static final String EXTRA_REQUEST_MADE = "com.lukekorth.fivehundredpx.fivehundredpxoauthactivity.EXTRA_REQUEST_MADE";

    private FiveHundredPxOAuthHelper mOAuthHelper;

    private OAuthSigningRequestTask mSigningRequestTask;
    private boolean mRequestMade;

    private OAuthAccessTokenRequestTask mAccessTokenRequestTask;
    private boolean mResponseReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        if (savedInstanceState != null) {
            mRequestMade = savedInstanceState.getBoolean(EXTRA_REQUEST_MADE);
        }

        mOAuthHelper = new FiveHundredPxOAuthHelper(BuildConfig.CONSUMER_KEY, BuildConfig.CONSUMER_SECRET);

        mSigningRequestTask = new OAuthSigningRequestTask();
        mSigningRequestTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestMade && !mResponseReceived) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri response = intent.getData();
        if (response.getScheme().equals(FiveHundredPxOAuthHelper.FIVE_HUNDRED_PX_CALLBACK_URL_SCHEME)) {
            mResponseReceived = true;
            mOAuthHelper.parseAuthorizationResponse(response);
            mAccessTokenRequestTask = new OAuthAccessTokenRequestTask();
            mAccessTokenRequestTask.execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_REQUEST_MADE, mRequestMade);
    }

    private class OAuthSigningRequestTask extends AsyncTask<Void, Void, Void> {

        private String mUrl;
        private FiveHundredException mException;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mUrl = mOAuthHelper.getAuthorizationUrl();
            } catch (OAuthNotAuthorizedException | OAuthExpectationFailedException | OAuthMessageSignerException |
                    OAuthCommunicationException e) {
                mException = new FiveHundredException(e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mException != null) {
                Intent resultIntent = new Intent().putExtra(EXCEPTION, mException);
                setResult(FAILED, resultIntent);
                finish();
            } else {
                mRequestMade = true;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    Bundle extras = new Bundle();
                    extras.putBinder("android.support.customtabs.extra.SESSION", null);
                    intent.putExtras(extras);
                }

                startActivity(intent);
            }
        }
    }

    private class OAuthAccessTokenRequestTask extends AsyncTask<Void, Void, Void> {

        private AccessToken mAccessToken;
        private FiveHundredException mException;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mAccessToken = mOAuthHelper.getAccessToken();
            } catch (OAuthCommunicationException | OAuthExpectationFailedException | OAuthMessageSignerException |
                    OAuthNotAuthorizedException e) {
                mException = new FiveHundredException(e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mException != null) {
                Intent resultIntent = new Intent().putExtra(EXCEPTION, mException);
                setResult(FAILED, resultIntent);
                finish();
            } else {
                Intent resultIntent = new Intent().putExtra(ACCESS_TOKEN, mAccessToken);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    }
}
