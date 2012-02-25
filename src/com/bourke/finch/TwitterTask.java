package com.bourke.finch;

import android.os.AsyncTask;

import android.util.Log;

import android.view.Window;

import android.widget.ImageView;
import android.widget.Toast;

import com.bourke.finch.lazylist.ImageLoader;

import java.util.List;

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
        AsyncTask<TwitterTask.Payload, Object, TwitterTask.Payload> {

    public static final String TAG = "Finch/TwitterTask";

    public static final int SHOW_USER = 0;
    public static final int GET_HOME_TIMELINE = 1;

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
                HomeActivity app = (HomeActivity)payload.data[0];
                app.setProgressBarIndeterminateVisibility(Boolean.TRUE);
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

                /* Perform the task */
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
        }

        return payload;
    }

    @Override
    public void onPostExecute(TwitterTask.Payload payload) {

        if (payload == null) {
            return;
        }

        HomeActivity app = null;

        switch(payload.taskType) {

            case SHOW_USER:
                String screenName = ((User)payload.result).getScreenName();
                app = (HomeActivity)payload.data[0];
                app.setProgressBarIndeterminateVisibility(false);
                app.getSupportActionBar().setSubtitle(screenName);
                break;

            case GET_HOME_TIMELINE:
                app = (HomeActivity)payload.data[0];
                app.setProgressBarIndeterminateVisibility(false);
                app.getMainList().setStatuses(
                        (ResponseList<twitter4j.Status>)payload.result);
                app.getMainList().notifyDataSetChanged();
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

