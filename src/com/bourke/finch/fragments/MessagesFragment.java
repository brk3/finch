package com.bourke.finch;

import android.content.Context;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;

import com.bourke.finch.common.FinchTwitterFactory;

import twitter4j.Twitter;

public class MessagesFragment extends SherlockFragment {

    private static final String TAG = "Finch/MessagesFragment";

    private Twitter mTwitter;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = getSherlockActivity().getApplicationContext();

		/* Load the twitter4j helper */
        mTwitter = FinchTwitterFactory.getInstance(mContext).getTwitter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater
            .inflate(R.layout.messages_fragment, container, false);

        //

        return layout;
    }
}
