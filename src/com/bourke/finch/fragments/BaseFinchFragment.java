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

import twitter4j.Status;

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

    protected long mSinceId = -1;

    protected long mMaxId = -1;

    protected boolean mLoadingPage = false;

    protected int mUnreadCount = 0;

    private int mTwitterTaskType;

    private FinchActivity mActivity;

    /* Update the unread display on scrolling every X items */
    private static final int UPDATE_UNREAD_COUNT_INTERVAL = 3;

    private static int FETCH_LIMIT = 20;

    public BaseFinchFragment(int twitterTaskType) {
        mTwitterTaskType = twitterTaskType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        setHasOptionsMenu(true);

        mActivity = (FinchActivity)getSherlockActivity();
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
            case R.id.menu_compose:
                mActivity.showDialog();
                return true;
            case R.id.menu_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void refresh() {
        Log.d(TAG, "refresh()");

        Paging page = new Paging();
        page.setCount(FETCH_LIMIT);
        if (mSinceId > -1) {
            page.setSinceId(mSinceId);
            Log.d(TAG, "sinceId=" + mSinceId + ", only fetching tweets after "
                    + "then");
        } else {
            Log.d(TAG, "No sinceId found, fetching first " + FETCH_LIMIT +
                    " tweets");
        }

        TwitterTaskCallback taskCallback = new TwitterTaskCallback
                <TwitterTaskParams, TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                ResponseList<TwitterResponse> res =
                    (ResponseList<TwitterResponse>)payload.result;
                if (res.size() == 0) {
                    Log.d(TAG, "res.size() == 0, no action");
                    return;
                }
                mMainListAdapter.prependResponses((ResponseList)res);
                mMainListAdapter.notifyDataSetChanged();

                mSinceId = ((Status)res.get(0)).getId();
                ResponseList<TwitterResponse> responseList =
                    mMainListAdapter.getResponses();
                mMaxId = ((Status)responseList.get(responseList.size()-1))
                    .getId();

                mUnreadCount = mMainList.getFirstVisiblePosition();
                mActivity.updateUnreadDisplay();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        TwitterTaskParams taskParams = new TwitterTaskParams(
                mTwitterTaskType, new Object[] {mActivity, mMainListAdapter,
                    mMainList, page});

        new TwitterTask(taskParams, taskCallback, mTwitter).execute();
    }

    protected void loadNextPage() {
        Log.d(TAG, "loadNextPage");
        if (mMaxId == -1) {
            Log.e(TAG, "loadNextPage: mMaxId == -1");
            return;
        }
        TwitterTaskCallback taskCallback = new TwitterTaskCallback
                <TwitterTaskParams, TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                ResponseList<TwitterResponse> res =
                    (ResponseList<TwitterResponse>)payload.result;
                if (res.size()-1 == 0) {  // -1 as maxId is inclusive
                    Log.d(TAG, "res.size()-1 == 0, no action");
                    return;
                }
                mMaxId = ((Status)res.get(res.size()-1)).getId();
                res.remove(0); // Avoid overlap with maxId
                mMainListAdapter.appendResponses(res);
                mMainListAdapter.notifyDataSetChanged();
                mLoadingPage = false;
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        Paging paging = new Paging();
        paging.setCount(FETCH_LIMIT);
        paging.setMaxId(mMaxId);
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

    public int getUnreadCount() {
        return mUnreadCount;
    }

    public abstract void setupActionMode();
}
