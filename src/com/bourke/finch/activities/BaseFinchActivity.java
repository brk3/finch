package com.bourke.finch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;

import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.View;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.finch.common.Constants;
import com.bourke.finch.common.FinchTwitterFactory;
import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;

import twitter4j.auth.AccessToken;

import twitter4j.ProfileImage;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.User;

public abstract class BaseFinchActivity extends SherlockFragmentActivity {

    private static final String TAG = "Finch/BaseFinchActivity";

    private MenuItem mMenuItemProgress;

    private MenuItem mMenuItemRefresh;

    private SharedPreferences mPrefs;

    private AccessToken mAccessToken;

    private Twitter mTwitter;

    private Context mContext;

    public View mActionCustomView;

    public TextView mUnreadCountView;

    public int mUnreadCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        mPrefs = getSharedPreferences("twitterPrefs", Context.MODE_PRIVATE);

        initActionBar();

        if (!initTwitter()) {
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            startActivity(intent);
        } else {
            showUserInActionbar();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);

        mMenuItemProgress = menu.findItem(R.id.menu_progress);
        mMenuItemRefresh = menu.findItem(R.id.menu_refresh);

        return true;
    }

    public void showProgressIcon(boolean show) {
        if (mMenuItemProgress != null && mMenuItemRefresh != null) {
            if(show) {
                mMenuItemProgress.setVisible(true);
                mMenuItemRefresh.setVisible(false);
            }
            else {
                mMenuItemRefresh.setVisible(true);
                mMenuItemProgress.setVisible(false);
            }
        }
    }

    private boolean initTwitter() {
        mPrefs = getSharedPreferences("twitterPrefs", Context.MODE_PRIVATE);
        String token = mPrefs.getString(Constants.PREF_ACCESS_TOKEN, null);
        String secret = mPrefs.getString(
                Constants.PREF_ACCESS_TOKEN_SECRET, null);
        if (token == null || secret == null) {
            return false;
        }
        mAccessToken = new AccessToken(token, secret);
        mTwitter = FinchTwitterFactory.getInstance(mContext)
            .getTwitter();
        mTwitter.setOAuthAccessToken(mAccessToken);
        FinchTwitterFactory.getInstance(mContext).setTwitter(mTwitter);
        return true;
    }

    private void initActionBar() {
        /*
         * Set up actionbar and split backgrounds / color
         * Workaround for http://b.android.com/15340
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(
                    R.drawable.bg_light_grey_stripe);
            bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setBackgroundDrawable(bg);
        }

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        mActionCustomView = getLayoutInflater().inflate(
                R.layout.actionbar_layout, null);
        mUnreadCountView = (TextView)mActionCustomView.findViewById(
                R.id.ab_text_unread_count);

        TextView titleTextView = (TextView)mActionCustomView.findViewById(
                R.id.ab_text_title);
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                Constants.SHADOWS_INTO_LIGHT_REG);
        titleTextView.setTypeface(typeface);
        titleTextView.setText("Finch");
        getSupportActionBar().setCustomView(mActionCustomView);
    }

    //TODO: this entire function badly needs to be cached
    private void showUserInActionbar() {
        /* Set up callback to set user's profile image to actionbar */
        final TwitterTaskCallback<TwitterTaskParams, TwitterException>
            profileImageCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                Drawable profileImage = (Drawable)payload.result;
                ImageView homeIcon = (ImageView)mActionCustomView
                    .findViewById(R.id.ab_home_icon);
                int abHeight = getSupportActionBar().getHeight();
                RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(abHeight, abHeight);
                layoutParams.setMargins(5, 5, 5, 5);
                homeIcon.setLayoutParams(layoutParams);
                homeIcon.setImageDrawable(profileImage);
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };

        TwitterTaskCallback<TwitterTaskParams, TwitterException>
            showUserCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                String screenName = ((User)payload.result).getScreenName();
                TextView textScreenName = (TextView)mActionCustomView
                    .findViewById(R.id.ab_text_screenname);
                textScreenName.setText("@"+screenName);

                /* Now we have screenName, start another thread to get the
                 * profile image */
                TwitterTaskParams showProfileImageParams =
                    new TwitterTaskParams(TwitterTask.GET_PROFILE_IMAGE,
                        //new Object[] {getCacheDir(), screenName,
                        new Object[] {BaseFinchActivity.this, screenName,
                            ProfileImage.NORMAL});
                new TwitterTask(showProfileImageParams, profileImageCallback,
                        mTwitter).execute();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };

        /* Set actionbar subtitle to user's username, and home icon to user's
         * profile image */
        TwitterTaskParams showUserParams = new TwitterTaskParams(
                TwitterTask.SHOW_USER,
                new Object[] {this, mAccessToken.getUserId()});
        new TwitterTask(showUserParams, showUserCallback,
                mTwitter).execute();
    }
}
