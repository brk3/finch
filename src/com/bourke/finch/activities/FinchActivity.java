package com.bourke.finch;

import android.content.Context;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragment;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;
import android.util.Log;

public class FinchActivity extends BaseFinchActivity
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Finch/FinchActivity";

    public static final int HOME_PAGE = 0;
    public static final int CONNECTIONS_PAGE = 1;

    private HomePageFragment mHomePageFragment = new HomePageFragment();

    private ConnectionsFragment mConnectionsFragment =
        new ConnectionsFragment();

    private int mCurrentlyShowingFragment;

    private Context mContext;

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
        Log.d(TAG, "onPageSelected()");
        switch (position) {
            case HOME_PAGE:
                mHomePageFragment.refresh();
            case CONNECTIONS_PAGE:
                mConnectionsFragment.refresh();
        }
    }

    public void updateUnreadDisplay(int fragmentId, int count) {
        /*
        Log.d(TAG, "Adding " + count + " to total unread");
        if (fragmentId != mCurrentlyShowingFragment) {
            Log.d(TAG, "fragmentId != mCurrentlyShowingFragment, skipping");
            return;
        }
        mUnreadCount += count;
        mUnreadCountView.setText(mUnreadCount+"");
        getSupportActionBar().setCustomView(mActionCustomView);
        if (mUnreadCount > 0) {
            TextView tabIndicatorTextView = (TextView)findViewById(
                    android.R.id.text1);
            tabIndicatorTextView.setShadowLayer(15, 0, 0,
                    Constants.COLOR_FINCH_YELLOW);
        }
        */
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
                    return mHomePageFragment;
                case CONNECTIONS_PAGE:
                    return mConnectionsFragment;
            }
            return null;
        }
    }
}
