package com.lukekorth.fivehundredpx;

import android.net.Uri;
import android.text.TextUtils;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class FiveHundredPxOAuthHelper {

    public static final String FIVE_HUNDRED_PX_CALLBACK_URL_SCHEME = "com.lukekorth.photopaper";

    private static final String FIVE_HUNDRED_PX_API_URL = "https://api.500px.com/v1";
    private static final String REQUEST_TOKEN_URL = FIVE_HUNDRED_PX_API_URL + "/oauth/request_token";
    private static final String ACCESS_TOKEN_URL = FIVE_HUNDRED_PX_API_URL + "/oauth/access_token";
    private static final String AUTHORIZE_URL = FIVE_HUNDRED_PX_API_URL + "/oauth/authorize";

    private CommonsHttpOAuthConsumer mOAuthConsumer;
    private CommonsHttpOAuthProvider mOAuthProvider;

    private String mVerifier;

    public FiveHundredPxOAuthHelper(String consumerKey, String consumerSecret) {
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " must be started with" +
                    "a non-empty consumerKey and consumerSecret");
        }

        mOAuthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        mOAuthProvider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL,
                ACCESS_TOKEN_URL, AUTHORIZE_URL);
    }

    public String getAuthorizationUrl() throws OAuthCommunicationException, OAuthExpectationFailedException,
            OAuthNotAuthorizedException, OAuthMessageSignerException {
        return mOAuthProvider.retrieveRequestToken(mOAuthConsumer, FIVE_HUNDRED_PX_CALLBACK_URL_SCHEME + "://login/");
    }

    public void parseAuthorizationResponse(Uri redirectedUri) {
        mVerifier = redirectedUri.getQueryParameter("oauth_verifier");
    }

    public AccessToken getAccessToken() throws OAuthCommunicationException, OAuthExpectationFailedException,
            OAuthNotAuthorizedException, OAuthMessageSignerException {
        mOAuthProvider.retrieveAccessToken(mOAuthConsumer, mVerifier);

        return new AccessToken(mOAuthConsumer.getToken(), mOAuthConsumer.getTokenSecret());
    }
}
