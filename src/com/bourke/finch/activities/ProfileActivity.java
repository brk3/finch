package com.bourke.finch;

import android.content.Context;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.Typeface;

import android.net.Uri;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import com.bourke.finch.common.Constants;
import com.bourke.finch.common.FinchTwitterFactory;
import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.TwitterTaskCallback;
import com.bourke.finch.common.TwitterTaskParams;

import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

import twitter4j.ProfileImage;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.User;

public class ProfileActivity extends BaseFinchActivity {

    private static final String TAG = "finch/ProfileActivity";

    //TODO: add to R.strings
    public static final String[] CONTENT = new String[] {
        "Tweets", "Following", "Followers" };

    public static final int TWEETS_PAGE = 0;
    public static final int FOLLOWING_PAGE = 1;
    public static final int FOLLOWERS_PAGE = 2;

    private Twitter mTwitter;

    private ImageView mProfileImage;

    private Context mContext;

    private String mScreenName = new String();

    private Typeface mTypeFace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile);

        mContext = getApplicationContext();

        mTypeFace = Typeface.createFromAsset(getAssets(),
                Constants.ROBOTO_REGULAR);

        /* Get the URL we are being asked to view */
        Uri uri = getIntent().getData();
        if ((uri == null) && (savedInstanceState != null)) {
            uri = Uri.parse(savedInstanceState.getString(Constants.KEY_URL));
        }

        /* Check uri appears to be in correct format */
        if ((uri == null) || (uri.getPathSegments().size() < 2)) {
            // TODO: recover
        }
        mScreenName = uri.getPathSegments().get(1).replaceFirst("@", "");

		/* Load the twitter4j helper */
        mTwitter = FinchTwitterFactory.getInstance(mContext).getTwitter();

        /* Set up TabPageIndicator and bind viewpager to it */
        FinchPagerAdapter adapter = new FinchPagerAdapter(
                getSupportFragmentManager());
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);
        TabPageIndicator indicator =
            (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);

        /* Get profile image for screenname and add to imageview */
        mProfileImage = (ImageView)findViewById(R.id.image_profile);
        TwitterTaskCallback<TwitterTaskParams, TwitterException>
            profileImageCallback =  new TwitterTaskCallback<TwitterTaskParams,
                                                    TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                mProfileImage.setImageDrawable((BitmapDrawable)payload.result);
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        TwitterTaskParams showUserParams = new TwitterTaskParams(
                TwitterTask.GET_PROFILE_IMAGE, new Object[] {this, mScreenName,
                    ProfileImage.ORIGINAL}
                );
        new TwitterTask(showUserParams, profileImageCallback,
                mTwitter).execute();

        /* Get user object */
        TwitterTaskCallback<TwitterTaskParams, TwitterException>
            userObjectCallback = new TwitterTaskCallback<TwitterTaskParams,
                                                         TwitterException>() {
            public void onSuccess(TwitterTaskParams payload) {
                ProfileActivity.this.populateProfileView((User)payload.result);
            }
            public void onFailure(TwitterException e) {
                e.printStackTrace();
            }
        };
        TwitterTaskParams userObjectParams = new TwitterTaskParams(
                TwitterTask.SHOW_USER,
                new Object[] {this, mScreenName});
        new TwitterTask(userObjectParams, userObjectCallback,
                mTwitter).execute();
    }

    //TODO: move all this to a fragment
    private void populateProfileView(User user) {
        /* Set textview for screenname */
        TextView textViewScreenName = (TextView)findViewById(
                R.id.text_screenname);
        textViewScreenName.setText("@"+user.getScreenName());
        textViewScreenName.setTypeface(mTypeFace);

        /* Set textview for description */
        TextView textViewDescription = (TextView)findViewById(
                R.id.text_description);
        textViewDescription.setText(user.getDescription());
        textViewDescription.setTypeface(mTypeFace);
    }

    public String getScreenName() {
        return mScreenName;
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
