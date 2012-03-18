package com.bourke.finch;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

public class FinchActivity extends BaseFinchActivity
        implements ActionBar.OnNavigationListener {

    private static final String TAG = "finch/FinchActivity";

    private static final int NUM_ITEMS = 2;

    public static final int HOME_PAGE = 0;
    public static final int CONNECTIONS_PAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        FinchPagerAdapter adapter = new FinchPagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int page) {
                //TODO: update nav spinner
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }

    public static class FinchPagerAdapter extends FragmentPagerAdapter {

        public FinchPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
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
