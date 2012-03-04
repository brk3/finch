package com.bourke.finch;

import android.app.Activity;

import android.content.Context;

import android.text.util.Linkify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bourke.finch.lazylist.ImageLoader;

import java.util.List;

import twitter4j.ResponseList;

import twitter4j.User;

public class UserListAdapter extends BaseAdapter {

    private Activity activity;

    private ResponseList<User> mUsers;

    private static LayoutInflater inflater = null;

    public ImageLoader imageLoader;


    public UserListAdapter(Activity a) {
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

        if (mUsers != null) {
            String screenName = mUsers.get(position).getScreenName();

            TextView text_screenname = (TextView)vi.findViewById(
                    R.id.text_screenname);
            text_screenname.setText(screenName);

            ImageView image_profile = (ImageView)vi.findViewById(
                    R.id.image_profile);
            imageLoader.displayImage(screenName, image_profile);
        }

        return vi;
    }

    public int getCount() {
        int count = 0;
        if (mUsers != null) {
            count = mUsers.size();
        }
        return count;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public void setUsers(ResponseList<User> users) {
        mUsers = users;
    }

}
