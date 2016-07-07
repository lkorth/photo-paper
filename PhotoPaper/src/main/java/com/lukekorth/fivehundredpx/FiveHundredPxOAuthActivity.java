package com.lukekorth.fivehundredpx;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.lukekorth.photo_paper.R;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class FiveHundredPxOAuthActivity extends Activity {

    public static final int FAILED = 500;
    public static final String CONSUMER_KEY = "com.lukekorth.fivehundredpx.fivehundredpxoauthactivity.CONSUMER_KEY";
    public static final String CONSUMER_SECRET = "com.lukekorth.fivehundredpx.fivehundredpxoauthactivity.CONSUMER_SECRET";
    public static final String ACCESS_TOKEN = "com.lukekorth.fivehundredpx.fivehundredpxoauthactivity.ACCESS_TOKEN";
    public static final String EXCEPTION = "com.lukekorth.fivehundredpx.fivehundredpxoauthactivity.EXCEPTION";

    private FiveHundredPxOAuthHelper mOAuthHelper;

    private OAuthSigningRequestTask mSigningRequestTask;
    private boolean mRequestMade;

    private OAuthAccessTokenRequestTask mAccessTokenRequestTask;
    private boolean mResponseReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        Intent startIntent = getIntent();
        String consumerKey = startIntent.getStringExtra(CONSUMER_KEY);
        String consumerSecret = startIntent.getStringExtra(CONSUMER_SECRET);

        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " must be started with" +
                    "a CONSUMER_KEY and CONSUMER_SECRET");
        }

        mOAuthHelper = new FiveHundredPxOAuthHelper(consumerKey, consumerSecret);

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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl)));
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
