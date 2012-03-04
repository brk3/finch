package com.bourke.finch;

import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterFactory;

import twitter4j.User;

public class ProfileFragment extends Fragment {

    private static final String TAG = "Finch/ProfileFragment";

    public static final int TYPE_TWEETS = 0;
    public static final int TYPE_FOLLOWING = 1;
    public static final int TYPE_FOLLOWERS = 2;
    private int mType = TYPE_TWEETS;

    private Twitter mTwitter;

    private ListView mMainList;
    private PullToRefreshListView mRefreshableMainList;

    private BaseAdapter mMainListAdapter;

    private ResponseList<Status> mTimeline;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

		/* Load the twitter4j helper */
        mTwitter = ((FinchApplication)
                getActivity().getApplication()).getTwitter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        RelativeLayout layout = (RelativeLayout)inflater
            .inflate(R.layout.pull_refresh_list, container, false);

        /* Setup ListView */
        mRefreshableMainList = (PullToRefreshListView)layout.findViewById(
                R.id.list);
        mMainList = mRefreshableMainList.getRefreshableView();

        /* Set up adapter depending on TYPE */
        switch (mType) {
            case TYPE_TWEETS:
                mMainListAdapter = new UserTimeLineAdapter(getActivity());
                mMainList.setAdapter(mMainListAdapter);
                mMainList.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
                        // TODO: implement onitemclick
                    }
                });
                getUserTweets();
                break;

            case TYPE_FOLLOWING:
                mMainListAdapter = new UserListAdapter(getActivity());
                mMainList.setAdapter(mMainListAdapter);
                mMainList.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
                        // TODO: implement onitemclick
                    }
                });
                getFollowingIds();
                break;

            case TYPE_FOLLOWERS:

                break;

            default:
                Log.e(TAG, "Invalid ProfileFragment type");
                return layout;
        }

        /* Set up refreshableMainList callback */
		mRefreshableMainList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });

        return layout;
    }

    private void getUserTweets() {

        TwitterTaskCallback<TwitterTaskParams, TwitterException>
            userTimelineCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                 TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                mTimeline = (ResponseList<Status>)payload.result;
                ((UserTimeLineAdapter)mMainListAdapter).setStatuses(mTimeline);
                mMainListAdapter.notifyDataSetChanged();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        String screenName = ((ProfileActivity)
                ProfileFragment.this.getActivity()).getScreenName();
        TwitterTaskParams userTimelineParams = new TwitterTaskParams(
                TwitterTask.GET_USER_TIMELINE,
                new Object[] {getActivity(), screenName});

        new TwitterTask(userTimelineParams, userTimelineCallback,
                mTwitter).execute();
    }

    private void getFollowingIds() {
        TwitterTaskCallback<TwitterTaskParams, TwitterException>
            followingIdsCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                 TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                long[] ids = ((IDs)payload.result).getIDs();
                /* Now we have ids, get user objects */
                getFollowingUsers(ids);
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        String screenName = ((ProfileActivity)
                ProfileFragment.this.getActivity()).getScreenName();
        TwitterTaskParams followIdsParams = new TwitterTaskParams(
                TwitterTask.GET_FOLLOWING_IDS,
                new Object[] {getActivity(), screenName});

        new TwitterTask(followIdsParams, followingIdsCallback,
                mTwitter).execute();
    }

    public void getFollowingUsers(long[] ids) {
        TwitterTaskCallback<TwitterTaskParams, TwitterException>
            followingUsersCallback = new TwitterTaskCallback<TwitterTaskParams,
                                 TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                ResponseList<User> users = (ResponseList<User>)payload.result;
                ((UserListAdapter)mMainListAdapter).setUsers(users);
                mMainListAdapter.notifyDataSetChanged();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        TwitterTaskParams followUsersParams = new TwitterTaskParams(
                TwitterTask.LOOKUP_USERS,
                new Object[] {getActivity(), ids});

        new TwitterTask(followUsersParams, followingUsersCallback,
                mTwitter).execute();
    }

    public ProfileFragment(int type) {
        mType = type;
    }

}
