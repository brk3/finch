package com.bourke.finch;

import android.app.Application;

import twitter4j.Twitter;

public class FinchApplication extends Application {

    private Twitter mTwitter;

    public static final int THEME_DARK = R.style.Theme_Finch;
    public static final int THEME_LIGHT = R.style.Theme_Finch_Light;

    protected static final String CALLBACK_URL = "finch-callback:///";

    protected static final String CONSUMER_KEY = "7QIjyQd3cA8c8jn80tRtqw";
    protected static final String CONSUMER_SECRET =
        "yREZDaGBZfIGnlNGce9m80jRUKbnkYhZGT7XZkFZqg";

	protected static final String PREF_ACCESS_TOKEN = "accessToken";
	protected static final String PREF_ACCESS_TOKEN_SECRET =
        "accessTokenSecret";

    public Twitter getTwitter() {
        return mTwitter;
    }

    public void setTwitter(Twitter twitter) {
        mTwitter = twitter;
    }

}
