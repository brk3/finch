package com.bourke.finch;

import android.os.AsyncTask;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterFactory;

import twitter4j.User;

/* AsyncTask<executeParams, publishProgressParams, resultType> */

public class TwitterTask extends
        AsyncTask<TwitterTask.Payload, Object, TwitterTask.Payload> {

    public static final String TAG = "Finch/TwitterTask";

    public static final int SHOWUSER = 0;

    @Override
    protected void onPreExecute() {
    }

    public TwitterTask.Payload doInBackground(TwitterTask.Payload... params) {

        TwitterTask.Payload payload = params[0];
        int taskType = payload.taskType;

        switch(taskType) {

            case SHOWUSER:
                /* Extract the parameters of the task from payload */
                long userId = (Long)payload.data[1];

                /* Perform the task */
                Twitter twitter = new TwitterFactory().getInstance();
                User user = null;
                try {
                    user = twitter.showUser(userId);
                } catch (TwitterException e) {

                }

                /* Return result of the task */
                payload.result = user;
                break;
        }

        return payload;
    }

    @Override
    public void onPostExecute(TwitterTask.Payload payload) {

        switch(payload.taskType) {

            case SHOWUSER:
                HomeActivity app = (HomeActivity)payload.data[0];
                String screenName = ((User)payload.result).getScreenName();
                app.getSupportActionBar().setSubtitle(screenName);
                break;
        }
    }

    @Override
    public void onProgressUpdate(Object... value) {

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

