package com.bourke.finch;

import com.bourke.finch.common.TwitterTask;

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
