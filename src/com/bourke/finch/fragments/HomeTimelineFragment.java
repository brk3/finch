package com.bourke.finch;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import android.view.View;

import android.widget.AdapterView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
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

import java.util.List;

import twitter4j.Status;

import twitter4j.TwitterException;

import twitter4j.TwitterResponse;

public class HomeTimelineFragment extends BaseTimelineFragment {

    private static final String TAG = "Finch/HomeTimelineFragment";

    private int mLastSelectedIndex;

    public HomeTimelineFragment() {
        super(TwitterTask.GET_HOME_TIMELINE);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause()");

        /* Save currently displayed tweets */
        try {
            FileOutputStream fos = mContext.openFileOutput(
                    Constants.PREF_HOMETIMELINE_PAGE, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(mMainListAdapter.getResponses());
            os.close();
            Log.d(TAG, "Wrote home timeline, size " +
                    mMainListAdapter.getResponses().size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = mPrefs.edit();
        List<TwitterResponse> content = mMainListAdapter.getResponses();
        editor.putInt(Constants.PREF_HOMETIMELINE_POS,
                mMainList.getFirstVisiblePosition());
        Log.d(TAG, "Wrote hometimeline pos: " +
                mMainList.getFirstVisiblePosition());

        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");

        /* Load last viewed tweets, if any */
        if (mMainListAdapter.getResponses() == null) {
            File cacheFile = mContext.getCacheDir();
            try {
                FileInputStream fis = mContext.openFileInput(
                        Constants.PREF_HOMETIMELINE_PAGE);
                ObjectInputStream ois = new ObjectInputStream(fis);
                List<TwitterResponse> listContents = (List<TwitterResponse>)
                    ois.readObject();
                if (listContents != null && listContents.size() > 0) {
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

        refresh();
    }

    @Override
    public void setupActionMode() {
        mMainList.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                /* Show action mode */
                mMode = getSherlockActivity().startActionMode(
                    new ActionModeTweet());
                mLastSelectedIndex = position;
                mMainListAdapter.setSelectedIndex(position);
                mMainListAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void favoriteTweet(final long statusId) {
        TwitterTaskCallback favoriteTweetCallback =
                new TwitterTaskCallback<TwitterTaskParams,
                                        TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                Status favStatus = (Status) payload.result;
                Log.d(TAG, "Favorite completed for tweet id: " +
                        favStatus.getId());
                mMainListAdapter.showFavStatus(favStatus);
                mMainListAdapter.notifyDataSetChanged();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        TwitterTaskParams favoriteTweetParams =
             new TwitterTaskParams(TwitterTask.CREATE_FAVORITE,
                 new Object[] {getSherlockActivity(), statusId});
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
            mMainListAdapter.setSelectedIndex(-1);
            mMainListAdapter.notifyDataSetChanged();
        }
    }
}
