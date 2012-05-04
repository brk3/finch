package com.bourke.finch;

import android.content.Context;

import android.graphics.Color;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.util.Log;

import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import com.bourke.finch.common.Constants;
import com.bourke.finch.fragments.NewTweetDialogFragment;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;
import android.view.View;

public class MainActivity extends BaseFinchActivity
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Finch/MainActivity";

    public static final int HOME_PAGE = 0;
    public static final int CONNECTIONS_PAGE = 1;

    private HomeTimelineFragment mHomeTimelineFragment = new HomeTimelineFragment();

    private ConnectionsTimelineFragment mConnectionsTimelineFragment =
        new ConnectionsTimelineFragment();

    private int mCurrentPage = HOME_PAGE;

    private Context mContext;

    private int mStackLevel = 0;

    //TODO: add to R.strings
    public static final String[] CONTENT = new String[] {
        "Home", "Connect" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");
        mContext = getApplicationContext();
        setContentView(R.layout.main);
        initViewPager();

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected");
        mCurrentPage = position;
        switch (position) {
            case HOME_PAGE:
                mHomeTimelineFragment.refresh();
                break;
            case CONNECTIONS_PAGE:
                mConnectionsTimelineFragment.refresh();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    public void updateUnreadDisplay() {
        Log.d(TAG, "updateUnreadDisplay");
        int unreadCount = 0;
        switch (mCurrentPage) {
            case HOME_PAGE:
                unreadCount = mHomeTimelineFragment.getUnreadCount();
                break;
            case CONNECTIONS_PAGE:
                unreadCount = mConnectionsTimelineFragment.getUnreadCount();
                break;
        }
        mUnreadCountView.setText(unreadCount+"");
        getSupportActionBar().setCustomView(mActionCustomView);
        TextView tabIndicatorTextView = (TextView)findViewById(
                android.R.id.text1);
        if (unreadCount > 0) {
            tabIndicatorTextView.setShadowLayer(15, 0, 0,
                    Constants.COLOR_FINCH_YELLOW);
        } else {
            tabIndicatorTextView.setShadowLayer(0, 0, 0, Color.BLACK);
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        FinchPagerAdapter adapter = new FinchPagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TabPageIndicator indicator =
            (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setOnPageChangeListener(this);
        indicator.setViewPager(viewPager);
    }

    public void showDialog() {
        mStackLevel++;

        FragmentTransaction ft = getSupportFragmentManager()
            .beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(
                "dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = NewTweetDialogFragment.newInstance(
                mStackLevel);
        newFragment.show(ft, "dialog");
    }

    class FinchPagerAdapter extends FragmentPagerAdapter
            implements TitleProvider {

        public FinchPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }

        @Override
        public String getTitle(int position) {
            return CONTENT[position % ProfileActivity.CONTENT.length]
                .toUpperCase();
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case HOME_PAGE:
                    return mHomeTimelineFragment;
                case CONNECTIONS_PAGE:
                    return mConnectionsTimelineFragment;
            }
            return null;
        }
    }

}
