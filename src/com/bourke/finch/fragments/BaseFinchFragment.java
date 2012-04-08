package com.bourke.finch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.drawable.Drawable;

import android.net.Uri;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.finch.common.Constants;
import com.bourke.finch.common.FinchTwitterFactory;
import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;
import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.provider.FinchProvider;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import twitter4j.auth.AccessToken;

import twitter4j.ProfileImage;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterResponse;

import twitter4j.User;
import android.widget.TextView;

public abstract class BaseFinchFragment extends SherlockFragment
        implements OnScrollListener {

    protected static final String TAG = "Finch/BaseFinchFragment";

    protected ListView mMainList;
    protected LazyAdapter mMainListAdapter;

    protected Twitter mTwitter;

    protected AccessToken mAccessToken;

    protected SharedPreferences mPrefs;

    protected ActionMode mMode;

    protected Context mContext;

    protected int mPage = 1;

    protected boolean mLoadingPage = false;

    private TextView mUnreadCountView;
    private int mUnreadCount = 0;

    private View mActionCustomView;

    /* Update the unread display on scrolling every X items */
    private static final int UPDATE_UNREAD_COUNT_INTERVAL = 3;

    protected long mSinceId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mContext = getSherlockActivity().getApplicationContext();
        mPrefs = getSherlockActivity().getSharedPreferences(
                "twitterPrefs", Context.MODE_PRIVATE);
        mTwitter = FinchTwitterFactory.getInstance(mContext).getTwitter();

        mActionCustomView = getSherlockActivity().getLayoutInflater()
            .inflate(R.layout.actionbar_layout, null);
        mUnreadCountView = (TextView)mActionCustomView.findViewById(
                R.id.text_unread_count);
        getSherlockActivity().getSupportActionBar().setCustomView(
                mActionCustomView);

        showUserInActionbar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        RelativeLayout layout = (RelativeLayout)inflater
            .inflate(R.layout.standard_list_fragment, container, false);
        initMainList(layout);

        return layout;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisible, int visibleCount,
            int totalCount) {

        if (totalCount <= 0 || mLoadingPage)
            return;

        boolean loadMore = firstVisible + visibleCount >= totalCount;

        if (loadMore) {
            mLoadingPage = true;
            loadNextPage();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView v, int scrollState) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initMainList(ViewGroup layout) {
        mMainList = (ListView)layout.findViewById(R.id.list);
        mMainListAdapter = new LazyAdapter(getSherlockActivity());
        mMainList.setAdapter(mMainListAdapter);
        mMainList.setOnScrollListener(this);
        setupActionMode();
    }

    private void showUserInActionbar() {
        //TODO: this entire function badly needs to be cached
        /* Set up callback to set user's profile image to actionbar */
        final TwitterTaskCallback<TwitterTaskParams, TwitterException>
            profileImageCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {

            public void onSuccess(TwitterTaskParams payload) {
                Drawable profileImage = (Drawable)payload.result;
                ImageView homeIcon = (ImageView)mActionCustomView
                    .findViewById(R.id.home_icon);
                int abHeight = getSherlockActivity().getSupportActionBar()
                    .getHeight();
                RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(abHeight, abHeight);
                layoutParams.setMargins(5, 5, 5, 5);
                homeIcon.setLayoutParams(new RelativeLayout.LayoutParams(
                            abHeight, abHeight));
                homeIcon.setImageDrawable(profileImage);
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

                /* Now we have screenName, start another thread to get the
                 * profile image */
                TwitterTaskParams showProfileImageParams =
                    new TwitterTaskParams(TwitterTask.GET_PROFILE_IMAGE,
                        new Object[] {getSherlockActivity(), screenName,
                            ProfileImage.NORMAL});
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
                new Object[] {getSherlockActivity(),
                    ((FinchActivity)getSherlockActivity()).getAccessToken()
                .getUserId()});
        new TwitterTask(showUserParams, showUserCallback,
                mTwitter).execute();
    }

    public void updateUnreadDisplay(int count) {
        Log.d(TAG, "Adding " + count + " to total unread");
        mUnreadCount += count;
        mUnreadCountView.setText(mUnreadCount+"");
        getSherlockActivity().getSupportActionBar().setCustomView(
                mActionCustomView);
    }

    public abstract void loadNextPage();
    public abstract void refresh();
    public abstract void setupActionMode();
}
