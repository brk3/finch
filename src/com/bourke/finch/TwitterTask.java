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

/*
 * Generic AsyncTask used to make API calls in a background thread.
 *
 * Pattern adapted from
 * http://jyro.blogspot.com/2009/11/android-asynctask-template.html
 *
 * AsyncTask<executeParams, publishProgressParams, resultType>
 */

public class TwitterTask extends
        AsyncTask<TwitterTask.Payload, Object, TwitterTask.Payload> {

    public static final String TAG = "Finch/TwitterTask";

    public static final int SHOW_USER = 0;
    public static final int GET_HOME_TIMELINE = 1;
    public static final int GET_PROFILE_IMAGE = 2;

    private Twitter mTwitter;

    private TwitterTask.Payload mParams;

    public TwitterTask(TwitterTask.Payload params, Twitter twitter) {
        mParams = params;
        mTwitter = twitter;
    }

    @Override
    protected void onPreExecute() {

        TwitterTask.Payload payload = mParams;
        int taskType = mParams.taskType;

        switch(taskType) {

            case SHOW_USER: case GET_HOME_TIMELINE:
                FinchActivity app = (FinchActivity)payload.data[0];
                app.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                break;

            case GET_PROFILE_IMAGE:
                break;
        }
    }

    public TwitterTask.Payload doInBackground(TwitterTask.Payload... params) {

        TwitterTask.Payload payload = mParams;
        int taskType = mParams.taskType;

        switch(taskType) {

            case SHOW_USER:
                /* Extract the parameters of the task from payload */
                long userId = (Long)payload.data[1];

                /* Perform the task(s) */
                User user = null;
                try {
                    user = mTwitter.showUser(userId);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }

                /* Return result of the task */
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
                    FinchActivity app = (FinchActivity)payload.data[0];
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
    public void onPostExecute(TwitterTask.Payload payload) {

        if (payload == null) {
            return;
        }

        FinchActivity app = null;

        switch(payload.taskType) {

            case SHOW_USER:
                String screenName = ((User)payload.result).getScreenName();
                app = (FinchActivity)payload.data[0];
                app.setProgressBarIndeterminateVisibility(false);
                app.getSupportActionBar().setSubtitle(screenName);
                SharedPreferences prefs = app.getSharedPreferences(
                        "twitterPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(FinchApplication.PREF_SCREEN_NAME,
                        screenName);
                editor.commit();

                /* Now we have screenName, start another thread to get the
                 * profile image */
                TwitterTask.Payload showProfileImageParams =
                    new TwitterTask.Payload(TwitterTask.GET_PROFILE_IMAGE,
                        new Object[] {app, screenName});
                new TwitterTask(showProfileImageParams, mTwitter).execute();
                break;

            case GET_HOME_TIMELINE:
                /* Stop spinner */
                app = (FinchActivity)payload.data[0];
                app.setProgressBarIndeterminateVisibility(false);

                /* Update list adapter */
                LazyAdapter mainListAdapter = (LazyAdapter)payload.data[1];
                mainListAdapter.setStatuses(
                        (ResponseList<twitter4j.Status>)payload.result);
                mainListAdapter.notifyDataSetChanged();

                /* Notify main list that it has been refreshed */
                PullToRefreshListView mainList =
                    (PullToRefreshListView)payload.data[2];
                mainList.onRefreshComplete();
                break;

            case GET_PROFILE_IMAGE:
                app = (FinchActivity)payload.data[0];
                Drawable profileImage = (Drawable)payload.result;
                ImageView homeIcon = (ImageView)app.findViewById(
                        android.R.id.home);
                int abHeight = app.getSupportActionBar().getHeight();
                homeIcon.setLayoutParams(new FrameLayout.LayoutParams(
                            abHeight, abHeight));
                homeIcon.setPadding(0, 10, 10, 10);
                homeIcon.setImageDrawable(profileImage);
                break;
        }
    }

    public static class Payload {
        public int taskType;
        public Object[] data;
        public Object result;
        public Object exception;

        public Payload(int taskType, Object[] data) {
            this.taskType = taskType;
            this.data = data;
        }
    }
}

