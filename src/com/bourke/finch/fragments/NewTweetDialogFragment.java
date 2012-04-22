package com.bourke.finch.fragments;

import android.content.Context;

import android.graphics.Color;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;

import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import com.bourke.finch.common.Constants;
import com.bourke.finch.R;
import android.widget.Button;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.util.Log;

public class NewTweetDialogFragment extends SherlockDialogFragment {

    private int mNum;

    private TextView mTextViewRemainingChars;

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

        mTextViewRemainingChars = (TextView)v.findViewById(
                R.id.textview_remaining_chars);
        mTextViewRemainingChars.setText(Constants.MAX_TWEET_LENGTH+"");

        EditText edittextCompose = (EditText)v.findViewById(
                R.id.edittext_compose);
        edittextCompose.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                int remainingChars = Constants.MAX_TWEET_LENGTH - s.length();
                mTextViewRemainingChars.setText(remainingChars+"");
                if (remainingChars < 0) {
                    mTextViewRemainingChars.setTextColor(Color.RED);
                } else {
                    mTextViewRemainingChars.setTextColor(Color.BLACK);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                int count) {}
        });

        Button buttonCancel = (Button)v.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NewTweetDialogFragment.this.getDialog().dismiss();
        }});

        return v;
    }
}
