package com.bourke.finch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.drawable.Drawable;

import android.net.Uri;

import android.os.Bundle;

import android.preference.PreferenceManager;

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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.provider.FinchProvider;

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

public class HomePageFragment extends SherlockFragment {

    private static final String TAG = "Finch/HomePageFragment";

    private ListView mMainList;
    private LazyAdapter mMainListAdapter;

    private Twitter mTwitter;

    private AccessToken mAccessToken;

    private SharedPreferences mPrefs;

    private TwitterTaskCallback
        <TwitterTaskParams, TwitterException> mHomeListCallback;

    private ResponseList<TwitterResponse> mHomeTimeline;

    private ActionMode mMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mPrefs = getSherlockActivity().getSharedPreferences(
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
                mHomeTimeline = (ResponseList<TwitterResponse>)payload.result;

                /* Update list adapter */
                mMainListAdapter.setResponses(mHomeTimeline);
                mMainListAdapter.notifyDataSetChanged();
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
            .inflate(R.layout.standard_list_fragment, container, false);

        /* Setup main ListView */
        mMainList = (ListView)layout.findViewById(R.id.list);
        mMainListAdapter = new LazyAdapter(getSherlockActivity());
        mMainList.setAdapter(mMainListAdapter);
        mMainList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                Intent profileActivity = new Intent(
                    HomePageFragment.this.getSherlockActivity(),
                    ProfileActivity.class);
                String screenName = (
                    (Status)mHomeTimeline.get(position)).getUser()
                    .getScreenName();
                profileActivity.setData(Uri.parse(FinchProvider.CONTENT_URI +
                        "/" + screenName));
                HomePageFragment.this.startActivity(profileActivity);
            }
        });
        mMainList.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mMode = getSherlockActivity().startActionMode(
                    new ActionModeTweet());
                return true;
            }
        });

        /* Setup prefs and check if credentials present */
		if (mPrefs.contains(FinchApplication.PREF_ACCESS_TOKEN)) {
			Log.d(TAG, "Repeat User");
			loginAuthorisedUser();
		} else {
			Log.d(TAG, "New User");
            Intent intent = new Intent();
            intent.setClass(getSherlockActivity(), LoginActivity.class);
            startActivity(intent);
		}

        return layout;
    }

    public void refreshHomeTimeline() {
        TwitterTaskParams getTimelineParams =
            new TwitterTaskParams(TwitterTask.GET_HOME_TIMELINE,
                    new Object[] {getSherlockActivity(), mMainListAdapter,
                        mMainList});
        new TwitterTask(getTimelineParams, mHomeListCallback,
                mTwitter).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshHomeTimeline();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        ((FinchApplication)getSherlockActivity().getApplication()).
            setTwitter(mTwitter);

        onLogin();
	}

    private void onLogin() {

		Toast.makeText(getSherlockActivity(), "Welcome back!",
                Toast.LENGTH_SHORT).show();

        /* Fetch user's timeline to populate ListView */
        TwitterTaskParams getTimelineParams = new TwitterTaskParams(
                TwitterTask.GET_HOME_TIMELINE,
                new Object[] {
                    getSherlockActivity(), mMainListAdapter, mMainList});
        new TwitterTask(getTimelineParams, mHomeListCallback,
                mTwitter).execute();

        final TwitterTaskCallback<TwitterTaskParams, TwitterException>
            profileImageCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {

            public void onSuccess(TwitterTaskParams payload) {
                Drawable profileImage = (Drawable)payload.result;
                ImageView homeIcon = (ImageView)HomePageFragment.this
                    .getSherlockActivity().findViewById(android.R.id.home);
                int abHeight = ((FinchActivity)HomePageFragment
                        .this.getSherlockActivity()).getSupportActionBar()
                        .getHeight();
                try {
                    homeIcon.setLayoutParams(new FrameLayout.LayoutParams(
                                abHeight, abHeight));
                    homeIcon.setPadding(0, 10, 10, 10);
                    homeIcon.setImageDrawable(profileImage);
                } catch (NullPointerException e) {
                    //TODO: Problem on <3.0, need to test further. Hopefully
                    //ABS 4.0 might fix this.
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
                ((FinchActivity)HomePageFragment.this.getSherlockActivity()).
                    getSupportActionBar().setSubtitle(screenName);
                SharedPreferences prefs =
                    HomePageFragment.this.getSherlockActivity()
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
                        new Object[] {getSherlockActivity(), screenName});
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
                new Object[] {getSherlockActivity(), mAccessToken.getUserId()
                });
        new TwitterTask(showUserParams, showUserCallback,
                mTwitter).execute();
    }

    private final class ActionModeTweet implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            /* Used to put dark icons on light action bar */
            boolean isLight = (FinchApplication.THEME ==
                FinchApplication.THEME_LIGHT);

            menu.add("Reply")
                .setIcon(isLight ? R.drawable.social_reply_light
                        : R.drawable.social_reply_dark)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menu.add("Re-tweet")
                .setIcon(isLight ? R.drawable.av_repeat_light
                        : R.drawable.av_repeat_dark)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menu.add("Favorite")
                .setIcon(isLight ? R.drawable.rating_not_important_light
                        : R.drawable.rating_not_important_dark)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menu.add("Share")
                .setIcon(isLight ? R.drawable.ic_action_share_light
                        : R.drawable.ic_action_share_dark)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}
