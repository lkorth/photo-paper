package com.lukekorth.android_500px.helpers;

import java.io.IOException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.HttpRequestAdapter;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;

public class SigningOkClient extends OkClient {

    private final OAuthConsumer mConsumer;

    public SigningOkClient(OAuthConsumer consumer) {
        mConsumer = consumer;
    }

    @Override
    public Response execute(Request request) throws IOException {
        Request requestToSend = request;
        try {
            HttpRequestAdapter signedAdapter = (HttpRequestAdapter) mConsumer.sign(request);
            requestToSend = (Request) signedAdapter.unwrap();
        } catch (OAuthMessageSignerException | OAuthExpectationFailedException |
                OAuthCommunicationException ignored) {}
        return super.execute(requestToSend);
    }
}
