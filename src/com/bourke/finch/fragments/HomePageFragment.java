package com.bourke.finch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.drawable.Drawable;

import android.net.Uri;

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

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.provider.FinchProvider;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import twitter4j.auth.AccessToken;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterFactory;

import twitter4j.TwitterResponse;

import twitter4j.User;

public class HomePageFragment extends Fragment {

    private static final String TAG = "Finch/HomePageFragment";

    private ListView mMainList;
    private PullToRefreshListView mRefreshableMainList;
    private LazyAdapter mMainListAdapter;

    private Twitter mTwitter;

    private AccessToken mAccessToken;

    private SharedPreferences mPrefs;

    private TwitterTaskCallback
        <TwitterTaskParams, TwitterException> mHomeListCallback;

    private ResponseList<TwitterResponse> mHomeTimeline;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mPrefs = getActivity().getSharedPreferences(
                "twitterPrefs", Context.MODE_PRIVATE);

		/* Load the twitter4j helper */
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(
                FinchApplication.CONSUMER_KEY);
		configurationBuilder.setOAuthConsumerSecret(
                FinchApplication.CONSUMER_SECRET);
		configurationBuilder.setUseSSL(true);
		Configuration configuration = configurationBuilder.build();
		mTwitter = new TwitterFactory(configuration).getInstance();

        /* Fetch user's hometimeline */
        mHomeListCallback = new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                /* Stop spinner */
                HomePageFragment.this.getActivity().
                    setProgressBarIndeterminateVisibility(false);

                mHomeTimeline = (ResponseList<TwitterResponse>)payload.result;

                /* Update list adapter */
                mMainListAdapter.setResponses(mHomeTimeline);
                mMainListAdapter.notifyDataSetChanged();

                /* Notify main list that it has been refreshed */
                mRefreshableMainList.onRefreshComplete();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
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
        mMainList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                Intent profileActivity = new Intent(
                    HomePageFragment.this.getActivity(),
                    ProfileActivity.class);
                // Minus one as list is not zero indexed
                String screenName = (
                    (Status)mHomeTimeline.get(position-1)).getUser()
                    .getScreenName();
                profileActivity.setData(Uri.parse(FinchProvider.CONTENT_URI +
                        "/" + screenName));
                HomePageFragment.this.startActivity(profileActivity);
            }
        });

        /* Set up refreshableMainList callback */
		mRefreshableMainList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                TwitterTaskParams getTimelineParams =
                     new TwitterTaskParams(TwitterTask.GET_HOME_TIMELINE,
                         new Object[] {getActivity(), mMainListAdapter,
                             mRefreshableMainList});
                new TwitterTask(getTimelineParams, mHomeListCallback,
                    mTwitter).execute();
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
        new TwitterTask(getTimelineParams, mHomeListCallback,
                mTwitter).execute();

        final TwitterTaskCallback<TwitterTaskParams, TwitterException>
            profileImageCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {

            public void onSuccess(TwitterTaskParams payload) {
                Drawable profileImage = (Drawable)payload.result;
                ImageView homeIcon = (ImageView)HomePageFragment.this
                    .getActivity().findViewById(android.R.id.home);
                int abHeight = ((FinchActivity)HomePageFragment.this.
                        getActivity()).getSupportActionBar().getHeight();
                try {
                    homeIcon.setLayoutParams(new FrameLayout.LayoutParams(
                                abHeight, abHeight));
                    homeIcon.setPadding(0, 10, 10, 10);
                    homeIcon.setImageDrawable(profileImage);
                } catch (NullPointerException e) {
                    /* Problem on <3.0, need to test further. Hopefully ABS 4.0
                     * might fix this. */
                    Log.e(TAG, "Could not get reference to home icon");
                    e.printStackTrace();
                }
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };

        TwitterTaskCallback<TwitterTaskParams, TwitterException>
            showUserCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                String screenName = ((User)payload.result).getScreenName();
                HomePageFragment.this.getActivity()
                    .setProgressBarIndeterminateVisibility(false);
                ((FinchActivity)HomePageFragment.this.getActivity()).
                    getSupportActionBar().setSubtitle(screenName);
                SharedPreferences prefs = HomePageFragment.this.getActivity()
                    .getSharedPreferences("twitterPrefs",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(FinchApplication.PREF_SCREEN_NAME,
                        screenName);
                editor.commit();

                /* Now we have screenName, start another thread to get the
                 * profile image */
                TwitterTaskParams showProfileImageParams =
                    new TwitterTaskParams(TwitterTask.GET_PROFILE_IMAGE,
                        new Object[] {getActivity(), screenName});
                new TwitterTask(showProfileImageParams, profileImageCallback,
                        mTwitter).execute();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };

        /* Set actionbar subtitle to user's username, and home icon to user's
         * profile image */
        TwitterTaskParams showUserParams = new TwitterTaskParams(
                TwitterTask.SHOW_USER,
                new Object[] {getActivity(), mAccessToken.getUserId()});
        new TwitterTask(showUserParams, showUserCallback,
                mTwitter).execute();
    }
}
