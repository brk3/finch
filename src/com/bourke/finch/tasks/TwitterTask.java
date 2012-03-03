package com.bourke.finch;

import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;

import android.support.v4.app.ActionBar;
import android.support.v4.view.MenuItem;

import android.util.Log;

import android.view.Window;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bourke.finch.lazylist.ImageLoader;
import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.lazylist.Utils;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;

import twitter4j.ProfileImage;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.User;
import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
import android.app.Activity;

/*
 * Generic AsyncTask used to make API calls in a background thread.
 *
 * Pattern adapted from
 * http://jyro.blogspot.com/2009/11/android-asynctask-template.html
 *
 * AsyncTask<executeParams, publishProgressParams, resultType>
 */

public class TwitterTask extends
        AsyncTask<TwitterTaskParams, Object, TwitterTaskParams> {

    public static final String TAG = "Finch/TwitterTask";

    public static final int SHOW_USER = 0;
    public static final int GET_HOME_TIMELINE = 1;
    public static final int GET_PROFILE_IMAGE = 2;

    private TwitterTaskParams mParams;

    private Twitter mTwitter;

    private TwitterTaskCallback mCallback;

    public TwitterTask(TwitterTaskParams params, TwitterTaskCallback callback,
            Twitter twitter) {

        mParams = params;
        mCallback = callback;
        mTwitter = twitter;
    }

    @Override
    protected void onPreExecute() {

        TwitterTaskParams payload = mParams;
        int taskType = mParams.taskType;

        switch(taskType) {

            case SHOW_USER: case GET_HOME_TIMELINE:
                Activity app = (Activity)payload.data[0];
                app.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                break;

            case GET_PROFILE_IMAGE:
                break;
        }
    }

    public TwitterTaskParams doInBackground(TwitterTaskParams... params) {

        TwitterTaskParams payload = mParams;
        int taskType = mParams.taskType;

        switch(taskType) {

            case SHOW_USER:
                User user = null;
                try {
                    Object userId = payload.data[1];
                    if (userId instanceof Long) {
                        user = mTwitter.showUser((Long)userId);
                    } else if (userId instanceof String) {
                        user = mTwitter.showUser((String)userId);
                    } else {
                        Log.e(TAG, "Error: TwitterTask.SHOW_USER called with "+
                                userId.getClass().getName());
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }
                payload.result = user;
                break;

            case GET_HOME_TIMELINE:
                List<twitter4j.Status> homeTimeline = null;
                try {
                    homeTimeline = mTwitter.getHomeTimeline();
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }
                payload.result = homeTimeline;
                break;

            case GET_PROFILE_IMAGE:
                //TODO: add caching
                Drawable bitmap = null;
                try {
                    Activity app = (Activity)payload.data[0];
                    String screenName = (String)payload.data[1];

                    ProfileImage p = mTwitter.getProfileImage(
                            screenName, ProfileImage.BIGGER);
                    String profileImageUrl = p.getURL();

                    File tempFile = new File(
                            app.getApplicationContext().getCacheDir(),
                            "profile_image");
                    URL imageUrl = new URL(p.getURL());
                    HttpURLConnection conn =
                        (HttpURLConnection)imageUrl.openConnection();
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setInstanceFollowRedirects(true);
                    InputStream is = conn.getInputStream();
                    OutputStream os = new FileOutputStream(tempFile);
                    Utils.CopyStream(is, os);
                    os.close();
                    bitmap = Drawable.createFromPath(tempFile.toString());
                } catch (TwitterException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                payload.result = bitmap;
                break;
        }

        return payload;
    }

    @Override
    public void onPostExecute(TwitterTaskParams payload) {

        if (payload == null) {
            return;
        }

        mCallback.onSuccess(payload);
    }

}

