package com.bourke.finch;

import android.content.Context;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragment;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class FinchActivity extends BaseFinchActivity {

    private static final String TAG = "Finch/FinchActivity";

    public static final int HOME_PAGE = 0;
    public static final int CONNECTIONS_PAGE = 1;

    private int mCurrentlyShowingFragment;

    private Context mContext;

    //TODO: add to R.strings
    public static final String[] CONTENT = new String[] {
        "Home", "Connect" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        setContentView(R.layout.main);
        initViewPager();
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
        indicator.setViewPager(viewPager);
    }

    public static class FinchPagerAdapter extends FragmentPagerAdapter
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
                    return new HomePageFragment();
                case CONNECTIONS_PAGE:
                    return new ConnectionsFragment();
            }
            return null;
        }
    }
}
