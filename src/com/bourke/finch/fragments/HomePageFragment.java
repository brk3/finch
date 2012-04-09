package com.bourke.finch;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import android.view.View;

import android.widget.AdapterView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.finch.common.Constants;
import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import twitter4j.Paging;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.TwitterException;

import twitter4j.TwitterResponse;
import com.actionbarsherlock.view.MenuInflater;

public class HomePageFragment extends BaseFinchFragment {

    private static final String TAG = "Finch/HomePageFragment";

    private int mLastSelectedIndex;

    @Override
    public void loadNextPage() {
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
    public void refresh() {
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

    public void setupActionMode() {
        mMainList.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mMode = getSherlockActivity().startActionMode(
                    new ActionModeTweet());
                mLastSelectedIndex = position;
                return false;
            }
        });
    }

    private void favoriteTweet(final long tweetId) {
        TwitterTaskCallback favoriteTweetCallback =
                new TwitterTaskCallback<TwitterTaskParams,
                                        TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                Log.d(TAG, "XXX: favorite completed for tweet id: " + tweetId);
                // TODO: set ribbon or some visual indication on tweet row that
                // this is now a favorite
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        TwitterTaskParams favoriteTweetParams =
             new TwitterTaskParams(TwitterTask.CREATE_FAVORITE,
                 new Object[] {getSherlockActivity(), tweetId});
        new TwitterTask(favoriteTweetParams, favoriteTweetCallback,
            mTwitter).execute();
    }

    private final class ActionModeTweet implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getSherlockActivity()
                .getSupportMenuInflater();
            inflater.inflate(R.menu.homepage_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_reply:
                    break;
                case R.id.menu_retweet:
                    break;
                case R.id.menu_favorite:
                    Status s = (Status) mMainListAdapter.getResponses().get(
                            mLastSelectedIndex);
                    if (s != null) {
                        favoriteTweet(s.getId());
                    } else {
                        Log.e(TAG, "Favorite failed, could not get entry " +
                                "position " + mLastSelectedIndex +
                                " from list");
                    }
                    break;
                case R.id.menu_share:
                    break;
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMainListAdapter.unselectLastView();
        }
    }

}
