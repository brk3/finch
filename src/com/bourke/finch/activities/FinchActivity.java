package com.bourke.finch;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import com.bourke.finch.common.Constants;
import com.bourke.finch.common.FinchTwitterFactory;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

import twitter4j.auth.AccessToken;

import twitter4j.Twitter;
import android.content.Intent;

public class FinchActivity extends BaseFinchActivity {

    private static final String TAG = "Finch/FinchActivity";

    public static final int HOME_PAGE = 0;
    public static final int CONNECTIONS_PAGE = 1;

    private AccessToken mAccessToken;

    private SharedPreferences mPrefs;

    private Context mContext;

    //TODO: add to R.strings
    public static final String[] CONTENT = new String[] {
        "Home", "Messages" };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mContext = getApplicationContext();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        FinchPagerAdapter adapter = new FinchPagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TabPageIndicator indicator =
            (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);

        if (!initTwitter()) {
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public boolean initTwitter() {
        mPrefs = getSharedPreferences("twitterPrefs", Context.MODE_PRIVATE);
        String token = mPrefs.getString(Constants.PREF_ACCESS_TOKEN, null);
        String secret = mPrefs.getString(
                Constants.PREF_ACCESS_TOKEN_SECRET, null);
        if (token == null || secret == null) {
            return false;
        }
        mAccessToken = new AccessToken(token, secret);
        Twitter twitter = FinchTwitterFactory.getInstance(mContext)
            .getTwitter();
        twitter.setOAuthAccessToken(mAccessToken);
        FinchTwitterFactory.getInstance(mContext).setTwitter(twitter);
        return true;
    }

    public AccessToken getAccessToken() {
        return mAccessToken;
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
            return CONTENT[ position % ProfileActivity.CONTENT.length]
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
