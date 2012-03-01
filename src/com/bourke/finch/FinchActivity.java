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

import java.util.ArrayList;

public class FinchActivity extends BaseFinchActivity
        implements ActionBar.OnNavigationListener {

    private static final int NUM_ITEMS = 2;

    public static final int HOME_PAGE = 0;
    public static final int MESSAGES_PAGE = 1;

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
        public Fragment getItem(int position) {
            switch (position) {
                case HOME_PAGE:
                    return new HomePageFragment();
                case MESSAGES_PAGE:
                    return new MessagesFragment();
            }
            return null;
        }
    }
}
