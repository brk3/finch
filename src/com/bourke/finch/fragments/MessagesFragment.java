package com.bourke.finch;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;

import twitter4j.Twitter;

import twitter4j.TwitterFactory;

public class MessagesFragment extends SherlockFragment {

    private static final String TAG = "Finch/MessagesFragment";

    private Twitter mTwitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

		/* Load the twitter4j helper */
		mTwitter = new TwitterFactory().getInstance();
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
