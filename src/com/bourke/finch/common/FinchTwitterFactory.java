package com.bourke.finch.common;

import android.content.Context;

import android.util.Log;

import com.bourke.finch.common.Constants;

import java.io.File;

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

        installHttpResponseCache();

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

    /**
     * Setup the Android 4.0 HttpResponseCache if on ICS or higher, otherwise
     * fall back to compatibility lib.
     */
    private void installHttpResponseCache() {
        final long httpCacheSize = 5 * 1024 * 1024; // 5 MiB
        final File httpCacheDir = new File("/sdcard", "http");

        try {
            Class.forName("android.net.http.HttpResponseCache")
                .getMethod("install", File.class, long.class)
                .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
            Log.d(TAG, "android.net.http.HttpResponseCache not available, " +
                    "probably because we're running on a pre-ICS version of " +
                    "Android. Using com.integralblue.httpresponsecache." +
                    "HttpHttpResponseCache.");
            try {
                com.integralblue.httpresponsecache.HttpResponseCache.install(
                        httpCacheDir, httpCacheSize);
            } catch(Exception e) {
                Log.e(TAG, "Failed to set up " +
                       "com.integralblue.httpresponsecache.HttpResponseCache");
                e.printStackTrace();
            }
        }
    }

    public Twitter getTwitter() {
        return mTwitter;
    }

    public void setTwitter(Twitter twitter) {
        mTwitter = twitter;
    }

}

