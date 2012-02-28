package com.bourke.finch;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import android.support.v4.app.Fragment;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class ProfileFragment extends Fragment {

    private static final String TAG = "Finch/ProfileFragment";

    private Twitter mTwitter;

    public ProfileFragment() {

    }

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
            .inflate(R.layout.standard_list_fragment, container, false);

        //

        return layout;
    }
}