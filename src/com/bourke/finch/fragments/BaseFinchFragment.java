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
import java.util.List;
import java.util.ArrayList;

public abstract class BaseFinchFragment extends SherlockFragment
        implements OnScrollListener {

    protected String TAG = "Finch/BaseFinchFragment";

    protected ListView mMainList;

    protected LazyAdapter mMainListAdapter;

    protected Twitter mTwitter;

    protected AccessToken mAccessToken;

    protected SharedPreferences mPrefs;

    protected ActionMode mMode;

    protected Context mContext;

    protected boolean mLoadingPage = false;

    protected int mUnreadCount = 0;

    private int mTwitterTaskType;

    private FinchActivity mActivity;

    /* Update the unread display on scrolling every X items */
    private static final int UPDATE_UNREAD_COUNT_INTERVAL = 3;

    private static int FETCH_LIMIT = 20;

    private List<TwitterResponse> mTimelineGap =
        new ResponseList();

    public BaseFinchFragment(int twitterTaskType) {
        mTwitterTaskType = twitterTaskType;

        switch (mTwitterTaskType) {
            case TwitterTask.GET_HOME_TIMELINE:
                TAG = "Finch/HomePageFragment";
                break;
            case TwitterTask.GET_MENTIONS:
                TAG = "Finch/ConnectionsFragment";
                break;
        }
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

        final Paging page = new Paging();
        page.setCount(FETCH_LIMIT);

        ResponseList currentList = mMainListAdapter.getResponses();
        mTimelineGap.clear();

        if (currentList == null || currentList.isEmpty()) {
            /* Fetch first N tweets and add to list */
            TwitterTaskCallback taskCallback = new TwitterTaskCallback
                    <TwitterTaskParams, TwitterException>() {
                public void onSuccess(TwitterTaskParams payload) {
                    ResponseList res = (ResponseList)payload.result;
                    if (res.size() == 0) {
                        Log.d(TAG, "res.size() == 0, no action");
                        return;
                    }
                    /* Update list */
                    mMainListAdapter.appendResponses((ResponseList)res);
                    mMainListAdapter.notifyDataSetChanged();
                    /* Update unread count display */
                    mUnreadCount = mMainList.getFirstVisiblePosition();
                    mActivity.updateUnreadDisplay();
                }
                public void onFailure(TwitterException e) {
                    e.printStackTrace();
                }
            };
            TwitterTaskParams taskParams = new TwitterTaskParams(
                    mTwitterTaskType, new Object[] {mActivity,
                        mMainListAdapter, mMainList, page});
            new TwitterTask(taskParams, taskCallback, mTwitter).execute();
        } else {
            page.setSinceId(((Status)currentList.get(0)).getId());
            final TwitterTaskCallback taskCallback = new TwitterTaskCallback
                    <TwitterTaskParams, TwitterException>() {
                public void onSuccess(TwitterTaskParams payload) {
                    ResponseList res = (ResponseList)payload.result;
                    if (res.size() > 0) {
                        mTimelineGap.addAll(res);
                        page.setMaxId(((Status)mTimelineGap.get(
                                        mTimelineGap.size()-1)).getId());
                        TwitterTaskParams taskParams = new TwitterTaskParams(
                                mTwitterTaskType, new Object[] {mActivity,
                                    mMainListAdapter, mMainList, page});
                        new TwitterTask(taskParams, this, mTwitter)
                            .execute();
                    } else {
                        mMainListAdapter.prependResponses(mTimelineGap);
                        mMainListAdapter.notifyDataSetChanged();
                    }
                }
                public void onFailure(TwitterException e) {
                    e.printStackTrace();
                }
            };
            TwitterTaskParams taskParams = new TwitterTaskParams(
                    mTwitterTaskType, new Object[] {mActivity,
                        mMainListAdapter, mMainList, page});
            new TwitterTask(taskParams, taskCallback, mTwitter).execute();
        }
    }

    protected void loadNextPage() {
        Log.d(TAG, "loadNextPage");

        ResponseList currentList = mMainListAdapter.getResponses();
        if (currentList.isEmpty()) {
            Log.e(TAG, "mMainListAdapter.getResponses() is empty");
            return;
        }
        TwitterTaskCallback taskCallback = new TwitterTaskCallback
                <TwitterTaskParams, TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                ResponseList res =
                    (ResponseList)payload.result;
                if (res.size()-1 == 0) {  // -1 as maxId is inclusive
                    Log.d(TAG, "res.size()-1 == 0, no action");
                    return;
                }
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
        long maxId = ((Status)currentList.get(currentList.size()-1)).getId();
        paging.setMaxId(maxId);
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
