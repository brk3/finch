package com.bourke.finch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.lazylist.TestAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import twitter4j.auth.AccessToken;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterFactory;

import twitter4j.User;

public class HomePageFragment extends Fragment {

    private static final String TAG = "Finch/HomePageFragment";

    private ListView mMainList;
    private PullToRefreshListView mRefreshableMainList;
    private LazyAdapter mMainListAdapter;

    private Twitter mTwitter;

    private AccessToken mAccessToken;

    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mPrefs = getActivity().getSharedPreferences(
                "twitterPrefs", Context.MODE_PRIVATE);

		/* Load the twitter4j helper */
		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(FinchApplication.CONSUMER_KEY,
                FinchApplication.CONSUMER_SECRET);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        RelativeLayout layout = (RelativeLayout)inflater
            .inflate(R.layout.pull_refresh_list, container, false);

        /* Setup main ListView */
        mRefreshableMainList = (PullToRefreshListView)layout.findViewById(
                R.id.list);
        mMainList = mRefreshableMainList.getRefreshableView();
        mMainListAdapter = new LazyAdapter(getActivity());
        mMainList.setAdapter(mMainListAdapter);

		mRefreshableMainList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                /* Fetch user's timeline to populate ListView */
                TwitterTaskCallback<TwitterTaskParams, TwitterException> tbc =
                        new TwitterTaskCallback<TwitterTaskParams,
                                                TwitterException>() {
                    public void onSuccess(TwitterTaskParams payload) {
                        /* Stop spinner */
                        //setProgressBarIndeterminateVisibility(false);

                        /* Update list adapter */
                        mMainListAdapter.setStatuses(
                                (ResponseList<twitter4j.Status>)
                                payload.result);
                        mMainListAdapter.notifyDataSetChanged();

                        /* Notify main list that it has been refreshed */
                        mRefreshableMainList.onRefreshComplete();
                    }
                    public void onFailure(TwitterException exception) {
                    }
                };
                TwitterTaskParams getTimelineParams =
                     new TwitterTaskParams(TwitterTask.GET_HOME_TIMELINE,
                         new Object[] {
                             getActivity(), mMainListAdapter,
                             mRefreshableMainList});
                new TwitterTask(getTimelineParams, tbc, mTwitter).execute();
            }
        });

        /* Setup prefs and check if credentials present */
		if (mPrefs.contains(FinchApplication.PREF_ACCESS_TOKEN)) {
			Log.d(TAG, "Repeat User");
			loginAuthorisedUser();
		} else {
			Log.d(TAG, "New User");
            Intent intent = new Intent();
            intent.setClass(getActivity(), LoginActivity.class);
            startActivity(intent);
		}

        return layout;
    }

    /*
     * The user had previously given our app permission to use Twitter.
	 */
	private void loginAuthorisedUser() {

		String token = mPrefs.getString(
                FinchApplication.PREF_ACCESS_TOKEN, null);
		String secret = mPrefs.getString(
                FinchApplication.PREF_ACCESS_TOKEN_SECRET, null);

		mAccessToken = new AccessToken(token, secret);
		mTwitter.setOAuthAccessToken(mAccessToken);
        ((FinchApplication)getActivity().getApplication()).
            setTwitter(mTwitter);

        onLogin();
	}

    private void onLogin() {

		Toast.makeText(getActivity(), "Welcome back!",
                Toast.LENGTH_SHORT).show();

        /* Fetch user's timeline to populate ListView */
        TwitterTaskParams getTimelineParams = new TwitterTaskParams(
                TwitterTask.GET_HOME_TIMELINE,
                new Object[] {
                    getActivity(), mMainListAdapter, mRefreshableMainList});
        new TwitterTask(getTimelineParams, null,  mTwitter).execute();

        TwitterTaskCallback<TwitterTaskParams, TwitterException> tbc =
                new TwitterTaskCallback<TwitterTaskParams,
                                        TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                /* Stop spinner */
                //setProgressBarIndeterminateVisibility(false);

                /* Update list adapter */
                mMainListAdapter.setStatuses(
                        (ResponseList<twitter4j.Status>)
                        payload.result);
                mMainListAdapter.notifyDataSetChanged();

                /* Notify main list that it has been refreshed */
                mRefreshableMainList.onRefreshComplete();
            }
            public void onFailure(TwitterException exception) {
            }
        };

        /* Set actionbar subtitle to user's username, and home icon to user's
         * profile image */
        TwitterTaskParams showUserParams = new TwitterTaskParams(
                TwitterTask.SHOW_USER,
                new Object[] {
                    getActivity(), mAccessToken.getUserId()});
        new TwitterTask(showUserParams, tbc, mTwitter).execute();
    }
}
