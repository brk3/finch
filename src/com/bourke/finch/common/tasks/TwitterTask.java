package com.bourke.finch.common;

import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;

import android.util.Log;

import android.view.Window;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.finch.BaseFinchActivity;
import com.bourke.finch.lazylist.ImageLoader;
import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.lazylist.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;

import twitter4j.IDs;

import twitter4j.ProfileImage;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.User;

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
    public static final int GET_USER_TIMELINE = 3;
    public static final int GET_FOLLOWING_IDS = 4;
    public static final int LOOKUP_USERS = 5;

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
        BaseFinchActivity app = (BaseFinchActivity)mParams.data[0];
        app.showProgressIcon(true);
    }

    @Override
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
                }
                payload.result = user;
                break;

            case GET_HOME_TIMELINE:
                List<twitter4j.Status> homeTimeline = null;
                try {
                    homeTimeline = mTwitter.getHomeTimeline();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                payload.result = homeTimeline;
                break;

            case GET_PROFILE_IMAGE:
                //TODO: add caching
                Drawable bitmap = null;
                try {
                    Activity app = (Activity)payload.data[0];
                    String p_screenName = (String)payload.data[1];

                    ProfileImage p = mTwitter.getProfileImage(
                            p_screenName, ProfileImage.ORIGINAL);
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

            case GET_USER_TIMELINE:
                List<twitter4j.Status> userTimeLine = null;
                String u_screenName = (String)payload.data[1];
                try {
                    userTimeLine = mTwitter.getUserTimeline(u_screenName);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                payload.result = userTimeLine;
                break;

            case GET_FOLLOWING_IDS:
                IDs ids = null;
                String f_screenName = (String)payload.data[1];
                try {
                    long cursor = -1; // begin paging
                    ids = mTwitter.getFollowersIDs(f_screenName, -1);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                payload.result = ids;
                break;

            case LOOKUP_USERS:
                ResponseList<User> users = null;
                long[] l_ids = (long[])payload.data[1];
                try {
                    users = mTwitter.lookupUsers(l_ids);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                payload.result = users;
                break;
        }

        return payload;
    }

    @Override
    public void onPostExecute(TwitterTaskParams payload) {
        if (payload == null || payload.result == null) {
            Log.e(TAG, "payload is null, returning");
            return;
        }

        BaseFinchActivity app = (BaseFinchActivity)mParams.data[0];
        app.showProgressIcon(false);

        mCallback.onSuccess(payload);
    }

}

