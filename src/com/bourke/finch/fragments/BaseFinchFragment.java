package com.bourke.finch;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.finch.common.FinchTwitterFactory;
import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;
import com.bourke.finch.lazylist.LazyAdapter;

import twitter4j.auth.AccessToken;

import twitter4j.Paging;

import twitter4j.ResponseList;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterResponse;

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

    private BaseFinchActivity mActivity;

    /* Update the unread display on scrolling every X items */
    private static final int UPDATE_UNREAD_COUNT_INTERVAL = 3;

    protected long mSinceId = -1;

    private int mTwitterTaskType;

    public BaseFinchFragment(int twitterTaskType) {
        mTwitterTaskType = twitterTaskType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        setHasOptionsMenu(true);

        mActivity = (BaseFinchActivity)getSherlockActivity();
        mContext = mActivity.getApplicationContext();
        mPrefs = mActivity.getSharedPreferences("twitterPrefs",
                Context.MODE_PRIVATE);
        mTwitter = FinchTwitterFactory.getInstance(mContext).getTwitter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

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

    protected void refresh() {
        Log.d(TAG, "refresh()");
        Paging page = new Paging(1);

        if (mSinceId > -1) {
            page.setSinceId(mSinceId);
            Log.d(TAG, "sinceId=" + mSinceId + ", only fetching tweets after "
                    + "then");
        } else {
            Log.d(TAG, "No sinceId found, fetching first page of tweets");
        }

        TwitterTaskParams taskParams = new TwitterTaskParams(
                mTwitterTaskType, new Object[] {mActivity, mMainListAdapter,
                    mMainList, page});

        TwitterTaskCallback taskCallback = new TwitterTaskCallback
                <TwitterTaskParams, TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                ResponseList<TwitterResponse> res =
                    (ResponseList<TwitterResponse>)payload.result;
                //updateUnreadDisplay(FinchActivity.HOME_PAGE,
                //        res.size());
                mMainListAdapter.prependResponses((ResponseList)res);
                mMainListAdapter.notifyDataSetChanged();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        new TwitterTask(taskParams, taskCallback, mTwitter).execute();
    }

    protected void loadNextPage() {
        TwitterTaskCallback taskCallback = new TwitterTaskCallback
                <TwitterTaskParams, TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                /* Append responses to list adapter */
                ResponseList<TwitterResponse> res =
                    (ResponseList<TwitterResponse>)payload.result;
                mMainListAdapter.appendResponses((ResponseList)res);
                mMainListAdapter.notifyDataSetChanged();
                mLoadingPage = false;
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        Paging paging = new Paging(++mPage);
        Log.d(TAG, "Fetching page " + mPage);
        TwitterTaskParams taskParams = new TwitterTaskParams(mTwitterTaskType,
                new Object[] {mActivity, mMainListAdapter, mMainList, paging});
        new TwitterTask(taskParams, taskCallback, mTwitter).execute();
    }

    private void initMainList(ViewGroup layout) {
        mMainList = (ListView)layout.findViewById(R.id.list);
        mMainListAdapter = new LazyAdapter(mActivity);
        mMainList.setAdapter(mMainListAdapter);
        mMainList.setOnScrollListener(this);
        setupActionMode();
    }

    public abstract void setupActionMode();
}
