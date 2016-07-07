package com.lukekorth.fivehundredpx;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class AccessToken implements Parcelable {

	private String mToken;
	private String mTokenSecret;

	public AccessToken(String token, String tokenSecret) {
		mToken = token;
		mTokenSecret = tokenSecret;
	}
	
	AccessToken(HttpResponse response) throws FiveHundredException {
		try {
			Uri responseUri = Uri.parse(EntityUtils.toString(response.getEntity()));

            mToken = responseUri.getQueryParameter("oauth_token");
			mTokenSecret = responseUri.getQueryParameter("oauth_token_secret");
		} catch (ParseException | IOException | NullPointerException e) {
            throw new FiveHundredException(e);
        }
    }

	public String getToken() {
		return mToken;
	}

	public String getTokenSecret() {
		return mTokenSecret;
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mToken);
        dest.writeString(mTokenSecret);
    }

    private AccessToken(Parcel in) {
        mToken = in.readString();
        mTokenSecret = in.readString();
    }

    public static final Creator<AccessToken> CREATOR = new Creator<AccessToken>() {
        public AccessToken createFromParcel(Parcel source) {
            return new AccessToken(source);
        }

        public AccessToken[] newArray(int size) {
            return new AccessToken[size];
        }
    };
}
