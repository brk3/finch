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

import android.util.Log;

import android.view.View;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import twitter4j.auth.AccessToken;

import twitter4j.ProfileImage;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.User;

public abstract class BaseFinchActivity extends SherlockFragmentActivity {

    private static final String TAG = "Finch/BaseFinchActivity";

    private MenuItem mMenuItemProgress;

    private MenuItem mMenuItemRefresh;

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

        if (!initTwitter()) {
            Intent intent = new Intent();
            intent.setClass(BaseFinchActivity.this,
                LoginActivity.class);
            startActivity(intent);
        } else {
            initActionBar();
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
        SharedPreferences twitterPrefs = getSharedPreferences(
                Constants.PREF_TOKEN_DATA, Context.MODE_PRIVATE);
        String token = twitterPrefs.getString(Constants.PREF_ACCESS_TOKEN,
                null);
        String secret = twitterPrefs.getString(
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
        /* Set up actionbar and split backgrounds / color
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

        /* Only show user info once actionbar is drawn to screen */
        ViewTreeObserver vto = mActionCustomView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mActionCustomView.getViewTreeObserver()
                    .removeGlobalOnLayoutListener(this);
                showUserInActionbar(false);
            }
        });
    }

    private void setActionBarHomeIcon(Drawable image) {
        ImageView homeIcon = (ImageView)mActionCustomView
            .findViewById(R.id.ab_home_icon);
        int abHeight = getSupportActionBar().getHeight();
        RelativeLayout.LayoutParams layoutParams =
            new RelativeLayout.LayoutParams(abHeight, abHeight);
        layoutParams.setMargins(5, 5, 5, 5);
        homeIcon.setLayoutParams(layoutParams);
        homeIcon.setImageDrawable(image);
    }

    private void setActionBarSubTitle(String text) {
        TextView textScreenName = (TextView)mActionCustomView
            .findViewById(R.id.ab_text_screenname);
        textScreenName.setText(text);
    }

    private void writeProfileCache(String profileImage, String screenName) {
        SharedPreferences profileData = getSharedPreferences(
                Constants.PREF_PROFILE_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = profileData.edit();
        editor.putString(Constants.PREF_PROFILE_IMAGE, profileImage);
        editor.putString(Constants.PREF_SCREEN_NAME, screenName);
        editor.commit();
        Log.d(TAG, "Sucessfully wrote profile cache data");
    }

    /* Check if both screenname and profileImage are in the cache */
    private boolean readProfileCache() {
        SharedPreferences profileData = getSharedPreferences(
                Constants.PREF_PROFILE_DATA, Context.MODE_PRIVATE);
        String screenName = profileData.getString(Constants.PREF_SCREEN_NAME,
                null);
        String profileImagePath = profileData.getString(
                Constants.PREF_PROFILE_IMAGE, null);
        if (profileImagePath != null && screenName != null) {
            Log.d(TAG, "Found profile data in cache, using it for ab home " +
                    "icon and subtitle");
            setActionBarSubTitle("@"+screenName);
            setActionBarHomeIcon(Drawable.createFromPath(profileImagePath));
            return true;
        }
        return false;
    }

    private void showUserInActionbar(boolean forceCacheRefresh) {
        if (!forceCacheRefresh) {
            boolean success = readProfileCache();
            if (success) {
                return;
            }
        }
        Log.d(TAG, "No profile data in cache, fetching it");
        /* If here we need to fetch them from Twitter */
        TwitterTaskCallback<TwitterTaskParams, TwitterException>
            showUserCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                final String screenName = ((User)payload.result)
                    .getScreenName();
                setActionBarSubTitle("@"+screenName);

                /* Now we have screenName, start another thread to get the
                 * profile image */
                final TwitterTaskCallback<TwitterTaskParams, TwitterException>
                    profileImageCallback = new TwitterTaskCallback
                        <TwitterTaskParams, TwitterException>() {
                    public void onSuccess(TwitterTaskParams payload) {
                        String imagePath = (String)payload.result;
                        Drawable profileImage = Drawable.createFromPath(
                                imagePath);
                        setActionBarHomeIcon(profileImage);
                        writeProfileCache(imagePath, screenName);
                    }
                    public void onFailure(TwitterException e) {
                        e.printStackTrace();
                    }
                };
                TwitterTaskParams showProfileImageParams =
                    new TwitterTaskParams(TwitterTask.GET_PROFILE_IMAGE,
                        new Object[] {BaseFinchActivity.this, screenName,
                            ProfileImage.NORMAL});
                new TwitterTask(showProfileImageParams, profileImageCallback,
                        mTwitter).execute();
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        TwitterTaskParams showUserParams = new TwitterTaskParams(
                TwitterTask.SHOW_USER,
                new Object[] {this, mAccessToken.getUserId()});
        new TwitterTask(showUserParams, showUserCallback, mTwitter).execute();
    }
}
