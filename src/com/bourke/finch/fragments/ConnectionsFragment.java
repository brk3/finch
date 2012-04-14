package com.bourke.finch;

import android.util.Log;

import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;

import twitter4j.Paging;

import twitter4j.ResponseList;

import twitter4j.TwitterException;

import twitter4j.TwitterResponse;

public class ConnectionsFragment extends BaseFinchFragment {

    private static final String TAG = "Finch/ConnectionsFragment";

    public ConnectionsFragment() {
        super(TwitterTask.GET_MENTIONS);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void setupActionMode() {
        // TODO
    }
}
