package com.bourke.finch.lazylist;

import android.app.Activity;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bourke.finch.R;

import twitter4j.ResponseList;

import twitter4j.Status;

import twitter4j.User;

import java.util.List;
import java.util.Collection;

public class LazyAdapter extends BaseAdapter {

    private Activity activity;
    private ResponseList<Status> mStatuses;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;

    public LazyAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.main_row, null);
        }

        if (mStatuses != null) {
            TextView text_tweet = (TextView)vi.findViewById(R.id.text_tweet);
            text_tweet.setText(mStatuses.get(position).getText());

            String screenName = mStatuses.get(position).getUser().
                getScreenName();

            TextView text_screenname =
                (TextView)vi.findViewById(R.id.text_screenname);
            text_screenname.setText("@"+screenName);

            ImageView image_profile = (ImageView)vi.findViewById(
                    R.id.image_profile);
            imageLoader.displayImage(screenName, image_profile);
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
