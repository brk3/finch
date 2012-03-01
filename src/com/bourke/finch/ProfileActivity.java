package com.bourke.finch;

import android.app.Activity;

import android.content.Context;

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

public class ProfileActivity extends BaseFinchActivity
        implements ActionBar.OnNavigationListener {

    //TODO: add to R.strings
    public static final String[] CONTENT = new String[] {
        "Tweets", "Following", "Followers" };

    public static final int TWEETS_PAGE = 0;
    public static final int FOLLOWING_PAGE = 1;
    public static final int FOLLOWERS_PAGE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Set layout and theme */
        setContentView(R.layout.profile);

        FinchPagerAdapter adapter = new FinchPagerAdapter(
                getSupportFragmentManager());

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);

        TabPageIndicator indicator =
            (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
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
