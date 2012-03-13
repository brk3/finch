package com.bourke.finch.common;

import android.content.Context;

import com.bourke.finch.common.Constants;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import twitter4j.Twitter;

import twitter4j.TwitterFactory;

public class FinchTwitterFactory {

    private static final String TAG = "RoidRage/ResourceLoader";

    private static FinchTwitterFactory singletonInstance = null;

    private Context mContext;

    private Twitter mTwitter;

    private FinchTwitterFactory(Context context) {
        mContext = context;

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(Constants.CONSUMER_KEY);
        configurationBuilder.setOAuthConsumerSecret(Constants.CONSUMER_SECRET);
        configurationBuilder.setUseSSL(true);
        Configuration configuration = configurationBuilder.build();
        mTwitter = new TwitterFactory(configuration).getInstance();
    }

    public static FinchTwitterFactory getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new FinchTwitterFactory(context);
        }
        return singletonInstance;
    }

    public Twitter getTwitter() {
        return mTwitter;
    }

    public void setTwitter(Twitter twitter) {
        mTwitter = twitter;
    }

}

