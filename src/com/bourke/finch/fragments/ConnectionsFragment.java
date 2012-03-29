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

import com.bourke.finch.common.Constants;
import com.bourke.finch.common.FinchTwitterFactory;
import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;
import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.provider.FinchProvider;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import twitter4j.auth.AccessToken;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import twitter4j.Paging;

import twitter4j.ProfileImage;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterResponse;

import twitter4j.User;

public class ConnectionsFragment extends SherlockFragment {

    private static final String TAG = "Finch/ConnectionsFragment";

    private ListView mMainList;
    private PullToRefreshListView mRefreshableMainList;
    private LazyAdapter mMainListAdapter;

    private Twitter mTwitter;

    private AccessToken mAccessToken;

    private SharedPreferences mPrefs;

    private TwitterTaskCallback
        <TwitterTaskParams, TwitterException> mMentionsCallback;

    private ResponseList<TwitterResponse> mMentions;

    private ActionMode mMode;

    private Context mContext;

    private int mMentionsPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = getSherlockActivity().getApplicationContext();
        mPrefs = getSherlockActivity().getSharedPreferences(
                "twitterPrefs", Context.MODE_PRIVATE);

        setHasOptionsMenu(true);

		/* Load the twitter4j helper */
        mTwitter = FinchTwitterFactory.getInstance(mContext).getTwitter();

        /* Fetch user's mentions */
        mMentionsCallback = new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                /* Update list adapter */
                mMentions = (ResponseList<TwitterResponse>)payload.result;
                mMainListAdapter.prependResponses((ResponseList)mMentions);
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
            .inflate(R.layout.pull_refresh_list, container, false);

        /* Setup main ListView */
        mRefreshableMainList = (PullToRefreshListView)layout.findViewById(
                R.id.list);
        mMainList = mRefreshableMainList.getRefreshableView();
        mMainListAdapter = new LazyAdapter(getSherlockActivity());
        mMainList.setAdapter(mMainListAdapter);
        mRefreshableMainList.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh() {
                /* Fetch user's timeline to populate ListView */
                TwitterTaskParams getMentionsParams =
                     new TwitterTaskParams(TwitterTask.GET_MENTIONS,
                         new Object[] {
                             getSherlockActivity(), mMainListAdapter,
                             mRefreshableMainList, new Paging(1)});
                new TwitterTask(getMentionsParams, mMentionsCallback,
                    mTwitter).execute();
			}

			@Override
			public void onPullUpToRefresh() {
                TwitterTaskCallback pullUpRefreshCallback =
                        new TwitterTaskCallback<TwitterTaskParams,
                                                TwitterException>() {
                    public void onSuccess(TwitterTaskParams payload) {
                        /* Append responses to list adapter */
                        mMentions = (ResponseList<TwitterResponse>)
                            payload.result;
                        mMainListAdapter.appendResponses((ResponseList)
                                mMentions);
                        mMainListAdapter.notifyDataSetChanged();
                    }
                    public void onFailure(TwitterException e) {
                        e.printStackTrace();
                    }
                };
                Paging paging = new Paging(++mMentionsPage);
                Log.d(TAG, "Fetching page " + mMentionsPage);
                TwitterTaskParams getMentionsParams =
                     new TwitterTaskParams(TwitterTask.GET_MENTIONS,
                         new Object[] {
                             getSherlockActivity(), mMainListAdapter,
                             mRefreshableMainList, paging});
                new TwitterTask(getMentionsParams, pullUpRefreshCallback,
                    mTwitter).execute();
            }
        });
        mMainList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                Intent profileActivity = new Intent(
                    ConnectionsFragment.this.getSherlockActivity(),
                    ProfileActivity.class);
                String screenName = (
                    (Status)mMentions.get(position)).getUser()
                        .getScreenName();
                profileActivity.setData(Uri.parse(FinchProvider.CONTENT_URI +
                        "/" + screenName));
                ConnectionsFragment.this.startActivity(profileActivity);
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
		if (mPrefs.contains(Constants.PREF_ACCESS_TOKEN)) {
			Log.d(TAG, "Repeat User");
			onLogin();
		} else {
			Log.d(TAG, "New User");
            Intent intent = new Intent();
            intent.setClass(getSherlockActivity(), LoginActivity.class);
            startActivity(intent);
		}

        return layout;
    }

    public void refreshMentions() {
        TwitterTaskParams getMentionsParams =
            new TwitterTaskParams(TwitterTask.GET_MENTIONS,
                    new Object[] {getSherlockActivity(), mMainListAdapter,
                        mRefreshableMainList, new Paging(1)});
        new TwitterTask(getMentionsParams, mMentionsCallback,
                mTwitter).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshMentions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onLogin() {

        /* Fetch user's mentions to populate ListView */
        TwitterTaskParams getTimelineParams = new TwitterTaskParams(
                TwitterTask.GET_MENTIONS, new Object[] {
                    getSherlockActivity(), mMainListAdapter,
                          mRefreshableMainList, new Paging(1)});
        new TwitterTask(getTimelineParams, mMentionsCallback, mTwitter)
            .execute();
    }

    private final class ActionModeTweet implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            /* Used to put dark icons on light action bar */
            //boolean isLight = (Constants.THEME == Constants.THEME_LIGHT);
            boolean isLight = true;

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
