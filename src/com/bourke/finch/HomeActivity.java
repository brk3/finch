package com.bourke.finch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;

import android.util.Log;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.lazylist.TestAdapter;

import twitter4j.auth.AccessToken;

import twitter4j.Twitter;

import twitter4j.TwitterFactory;

public class HomeActivity extends FragmentActivity
        implements ActionBar.OnNavigationListener {

    private static final String TAG = "Finch/HomeAcivity";

    private String[] mLocations;

    private ListView mMainList;
    private LazyAdapter mMainListAdapter;

    private Twitter mTwitter;

    private AccessToken mAccessToken;

    private SharedPreferences mPrefs;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

		/* Load the twitter4j helper */
		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(Constants.CONSUMER_KEY,
                Constants.CONSUMER_SECRET);

        /* Set layout and theme */
        setTheme(Constants.THEME_LIGHT);
        setContentView(R.layout.main);

        /* Set up actionbar navigation spinner */
        ArrayAdapter<CharSequence> list =
            ArrayAdapter.createFromResource(this, R.array.locations,
                R.layout.spinner_title);
        list.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        getSupportActionBar().setNavigationMode(
                ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);

        /* Setup main ListView */
        mMainList = (ListView)findViewById(R.id.list);
        String[] testTweets = {"Loren Ipsum", "Loren Ipsum", "Loren Ipsum"};
        TestAdapter adapter = new TestAdapter(this, testTweets);
        mMainList.setAdapter(adapter);
        //adapter = new LazyAdapter(this, mStrings);
        //mMainList.setAdapter(adapter);

        /* Setup prefs and check if credentials present */
        mPrefs = getSharedPreferences("twitterPrefs", MODE_PRIVATE);
		if (mPrefs.contains(Constants.PREF_ACCESS_TOKEN)) {
			Log.d(TAG, "Repeat User");
			loginAuthorisedUser();
		} else {
			Log.d(TAG, "New User");
            startActivity(new Intent(this, LoginActivity.class));
		}
    }

    /*
     * The user had previously given our app permission to use Twitter.
	 */
	private void loginAuthorisedUser() {
		String token = mPrefs.getString(Constants.PREF_ACCESS_TOKEN, null);
		String secret = mPrefs.getString(Constants.PREF_ACCESS_TOKEN_SECRET,
                null);

		mAccessToken = new AccessToken(token, secret);
		mTwitter.setOAuthAccessToken(mAccessToken);

        /* Set actionbar subtitle to user's username */
        TwitterTask.Payload showUserParams = new TwitterTask.Payload(
                TwitterTask.SHOWUSER, new Object[] {HomeActivity.this,
                    mAccessToken.getUserId()});
        new TwitterTask().execute(showUserParams);

		Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
	}

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Write")
            .setIcon(R.drawable.ic_action_edit)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add("Refresh")
            .setIcon(R.drawable.ic_action_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add("Search")
            .setIcon(R.drawable.ic_action_search)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

}
