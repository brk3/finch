package com.bourke.finch;

import android.content.SharedPreferences;

import android.util.Log;

import com.bourke.finch.common.Constants;
import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;

import twitter4j.Paging;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.TwitterException;

import twitter4j.TwitterResponse;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;
import java.io.StreamCorruptedException;
import java.io.IOException;
import java.io.File;
import android.content.Context;
import java.io.ObjectOutputStream;

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

        if (mSinceId > -1) {
            page.setSinceId(mSinceId);
            Log.d(TAG, "sinceId=" + mSinceId + ", only fetching tweets after "
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
                updateUnreadDisplay(res.size());
                mMainListAdapter.prependResponses((ResponseList)res);
                mMainListAdapter.notifyDataSetChanged();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };

        new TwitterTask(getTimelineParams, mHomeListCallback,
                mTwitter).execute();
    }

    @Override
    public void onPause() {
        super.onPause();

        /* Save currently displayed tweets */
        try {
            FileOutputStream fos = mContext.openFileOutput(
                    Constants.PREF_HOMETIMELINE_PAGE, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(mMainListAdapter.getResponses());
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = mPrefs.edit();
        ResponseList<TwitterResponse> content = mMainListAdapter
                .getResponses();
        if (content != null && content.size() > 0) {
            long sinceId = ((Status)content.get(0)).getId();
            editor.putLong(Constants.PREF_HOMETIMELINE_SINCEID, sinceId);
        }
        editor.putInt(Constants.PREF_HOMETIMELINE_POS,
                mMainList.getFirstVisiblePosition());
        editor.commit();
    }

    public void onResume() {
        super.onResume();

        /* Load last viewed tweets, if any */
        if (mMainListAdapter.getResponses() == null) {
            File cacheFile = mContext.getCacheDir();
            try {
                FileInputStream fis = mContext.openFileInput(
                        Constants.PREF_HOMETIMELINE_PAGE);
                ObjectInputStream ois = new ObjectInputStream(fis);
                ResponseList<TwitterResponse> listContents =
                    (ResponseList<TwitterResponse>) ois.readObject();
                if (listContents != null) {
                    mMainListAdapter.appendResponses(listContents);
                    mMainListAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Restored hometimeline");
                }
                ois.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /* Restore list position */
        int timelinePos = mPrefs.getInt(Constants.PREF_HOMETIMELINE_POS, -1);
        if (timelinePos != -1) {
            mMainList.setSelection(timelinePos);
            mMainListAdapter.notifyDataSetChanged();
        }

        mSinceId = mPrefs.getLong(Constants.PREF_HOMETIMELINE_SINCEID, -1);

        refresh();
    }

}
