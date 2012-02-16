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

/* Just a simple copy of LazyAdapter to try and help get the layout right */

public class TestAdapter extends BaseAdapter {

    private Activity activity;
    private String[] data;
    private static LayoutInflater inflater = null;

    public TestAdapter(Activity a, String[] d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View vi=convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.main_row, null);
        }

        TextView text_tweet = (TextView)vi.findViewById(R.id.text_tweet);
        text_tweet.setText("Loren Ipsum");

        ImageView image_profile = (ImageView)vi.findViewById(
                R.id.image_profile);

        return vi;
    }
}
