package com.bourke.finch;

import android.app.Activity;

import android.content.Context;

import android.graphics.Typeface;

import android.text.util.Linkify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bourke.finch.common.Constants;
import com.bourke.finch.R;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import twitter4j.ResponseList;

import twitter4j.Status;

public class UserTimeLineAdapter extends BaseAdapter {

    private Activity activity;

    private ResponseList<Status> mStatuses;

    private static LayoutInflater inflater = null;

    private Typeface mTypeface;

    public UserTimeLineAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mTypeface = Typeface.createFromAsset(a.getAssets(),
                Constants.ROBOTO_REGULAR);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.timeline_row, null);
        }

        if (mStatuses != null) {
            TextView text_tweet = (TextView)vi.findViewById(R.id.text_tweet);
            text_tweet.setText(mStatuses.get(position).getText());
            text_tweet.setTypeface(mTypeface);
        }

        return vi;
    }

    public int getCount() {
        int count = 0;
        if (mStatuses != null) {
            count = mStatuses.size();
        }
        return count;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public void setStatuses(ResponseList<Status> data) {
        mStatuses = data;
    }

}
