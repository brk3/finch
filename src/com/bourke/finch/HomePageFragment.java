package com.bourke.finch;

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

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.lazylist.TestAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import twitter4j.auth.AccessToken;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.Twitter;

import twitter4j.TwitterFactory;

import twitter4j.User;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class HomePageFragment extends Fragment {

    private static final String TAG = "Finch/HomePageFragment";

    private ListView mMainList;
    private LazyAdapter mMainListAdapter;

    private Twitter mTwitter;

    private AccessToken mAccessToken;

    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

		/* Load the twitter4j helper */
		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(FinchApplication.CONSUMER_KEY,
                FinchApplication.CONSUMER_SECRET);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        RelativeLayout layout = (RelativeLayout)inflater
            .inflate(R.layout.home_page, container, false);

        /* Setup main ListView */
        mMainList = (ListView)layout.findViewById(R.id.list);
        mMainListAdapter = new LazyAdapter(getActivity());
        mMainList.setAdapter(mMainListAdapter);

        /* Setup prefs and check if credentials present */
        mPrefs = getActivity().getSharedPreferences(
                "twitterPrefs", Context.MODE_PRIVATE);
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

        /* Set actionbar subtitle to user's username */
        TwitterTask.Payload showUserParams = new TwitterTask.Payload(
                TwitterTask.SHOW_USER,
                new Object[] {
                    getActivity(), mAccessToken.getUserId()});
        new TwitterTask(showUserParams, mTwitter).execute();

		Toast.makeText(getActivity(), "Welcome back!",
                Toast.LENGTH_SHORT).show();

        /* Fetch user's timeline to populate ListView */
        TwitterTask.Payload getTimelineParams = new TwitterTask.Payload(
                TwitterTask.GET_HOME_TIMELINE,
                new Object[] {getActivity(), mMainListAdapter});
        new TwitterTask(getTimelineParams, mTwitter).execute();
    }

}
