package com.bourke.finch.fragments;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import com.bourke.finch.R;

public class NewTweetDialogFragment extends SherlockDialogFragment {

    private int mNum;

    public static NewTweetDialogFragment newInstance(int num) {
        return new NewTweetDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog,
                container, false);

        return v;
    }
}
