package com.bourke.finch;

import android.app.Activity;

import android.content.Context;

import android.net.Uri;

import android.os.Bundle;
import android.os.Parcelable;

import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.Window;

import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bourke.finch.lazylist.LazyAdapter;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

import java.util.ArrayList;

import twitter4j.Twitter;

import twitter4j.TwitterFactory;
import android.util.Log;

public class ProfileActivity extends BaseFinchActivity
        implements ActionBar.OnNavigationListener {

    private static final String TAG = "finch/ProfileActivity";

    //TODO: add to R.strings
    public static final String[] CONTENT = new String[] {
        "Tweets", "Following", "Followers" };

    public static final int TWEETS_PAGE = 0;
    public static final int FOLLOWING_PAGE = 1;
    public static final int FOLLOWERS_PAGE = 2;

    private Twitter mTwitter;

    private Uri mURI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile);

        /* Get the URL we are being asked to view */
        Uri uri = getIntent().getData();
        if ((uri == null) && (savedInstanceState != null)) {
            uri = Uri.parse(savedInstanceState.getString(
                        FinchApplication.KEY_URL));
        }

        /* Check uri appears to be in correct format */
        if ((uri == null) || (uri.getPathSegments().size() < 2)) {
            // TODO: recover
        }
        mURI = uri;

		/* Load the twitter4j helper */
		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(FinchApplication.CONSUMER_KEY,
                FinchApplication.CONSUMER_SECRET);
        /* Set up TabPageIndicator and bind viewpager to it */
        FinchPagerAdapter adapter = new FinchPagerAdapter(
                getSupportFragmentManager());
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);
        TabPageIndicator indicator =
            (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);

        /* Get details for screenname and display */
        /*
        String screenName = uri.getPathSegments().get(1).replaceFirst("@", "");
        TwitterTaskPayload showUserParams = new TwitterTaskPayload(
                TwitterTask.SHOW_USER,
                new Object[] {getActivity(), mAccessToken.getUserId()});
        new TwitterTask(showUserParams, mTwitter).execute();
        */
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }

    public static class FinchPagerAdapter extends FragmentPagerAdapter
            implements TitleProvider {

        public FinchPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return ProfileActivity.CONTENT.length;
        }

        @Override
        public String getTitle(int position) {
            return ProfileActivity.CONTENT[
                position % ProfileActivity.CONTENT.length].toUpperCase();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TWEETS_PAGE:
                    return new ProfileFragment(ProfileFragment.TYPE_TWEETS);
                case FOLLOWING_PAGE:
                    return new ProfileFragment(ProfileFragment.TYPE_FOLLOWING);
                case FOLLOWERS_PAGE:
                    return new ProfileFragment(ProfileFragment.TYPE_FOLLOWERS);
            }
            return null;
        }
    }
}
