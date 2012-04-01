package com.bourke.finch;

import android.util.Log;

import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;

import twitter4j.Paging;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.TwitterException;

import twitter4j.TwitterResponse;
import java.util.List;

public class HomePageFragment extends BaseFinchFragment {

    private static final String TAG = "Finch/HomePageFragment";

    @Override
    protected void loadNextPage() {
        TwitterTaskCallback pullUpRefreshCallback =
                new TwitterTaskCallback<TwitterTaskParams,
                                        TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                /* Append responses to list adapter */
                ResponseList<TwitterResponse> res =
                    (ResponseList<TwitterResponse>)payload.result;
                mListContents = res;
                mMainListAdapter.appendResponses((ResponseList)mListContents);
                mMainListAdapter.notifyDataSetChanged();
                mLoadingPage = false;
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        Paging paging = new Paging(++mPage);
        Log.d(TAG, "Fetching page " + mPage);
        TwitterTaskParams getTimelineParams =
             new TwitterTaskParams(TwitterTask.GET_HOME_TIMELINE,
                 new Object[] {getSherlockActivity(), mMainListAdapter,
                     mMainList, paging});
        new TwitterTask(getTimelineParams, pullUpRefreshCallback,
            mTwitter).execute();
    }

    @Override
    protected void refresh() {
        Paging page = new Paging(1);
        if (mListContents != null) {
            long sinceId = ((Status)mListContents.get(0)).getId();
            page.setSinceId(sinceId);
            Log.d(TAG, "sinceId=" + sinceId + ", only fetching tweets since "
                    + "then");
        } else {
            Log.d(TAG, "No sinceId found, fetching first page of tweets");
        }

        TwitterTaskParams getTimelineParams =
            new TwitterTaskParams(TwitterTask.GET_HOME_TIMELINE,
                    new Object[] {getSherlockActivity(), mMainListAdapter,
                        mMainList, page});

        TwitterTaskCallback mHomeListCallback = new TwitterTaskCallback
                <TwitterTaskParams, TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                ResponseList<TwitterResponse> res =
                    (ResponseList<TwitterResponse>)payload.result;
                mListContents = res;
                mMainListAdapter.prependResponses((ResponseList)mListContents);
                mMainListAdapter.notifyDataSetChanged();
                updateUnreadDisplay(res.size());
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };

        new TwitterTask(getTimelineParams, mHomeListCallback,
                mTwitter).execute();
    }
}
