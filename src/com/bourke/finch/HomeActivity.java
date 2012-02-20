package com.bourke.finch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;

import android.util.Log;

import android.webkit.WebView;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.lazylist.TestAdapter;

import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterFactory;
import android.os.AsyncTask;

public class HomeActivity extends FragmentActivity
        implements ActionBar.OnNavigationListener {

    private static final String TAG = "Finch/HomeAcivity";

    protected static final String CONSUMER_KEY = "7QIjyQd3cA8c8jn80tRtqw";
    protected static final String CONSUMER_SECRET =
        "yREZDaGBZfIGnlNGce9m80jRUKbnkYhZGT7XZkFZqg";
    protected static final String CALLBACK_URL = "finch-callback:///";

	private static final String PREF_ACCESS_TOKEN = "accessToken";
	private static final String PREF_ACCESS_TOKEN_SECRET = "accessTokenSecret";
    private SharedPreferences mPrefs;

    public static int THEME_DARK = R.style.Theme_Finch;
    public static int THEME_LIGHT = R.style.Theme_Finch_Light;

    private String[] mLocations;

    private ListView mMainList;
    private LazyAdapter mMainListAdapter;

    private Twitter mTwitter;
	private RequestToken mReqToken;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

		/* Load the twitter4j helper */
		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);

        /* Set layout and theme */
        setTheme(THEME_LIGHT);
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
		if (mPrefs.contains(PREF_ACCESS_TOKEN)) {
			Log.d(TAG, "Repeat User");
			loginAuthorisedUser();
		} else {
			Log.d(TAG, "New User");
            new AuthTask().execute();
		}

        /* Set actionbar subtitle to user's username */
        getSupportActionBar().setSubtitle("@brk3");
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		dealWithTwitterResponse(intent);
	}

    /*
     * The user had previously given our app permission to use Twitter.
     * Therefore we retrieve these credentials and fill out the Twitter4j
     * helper.
	 */
	private void loginAuthorisedUser() {
		String token = mPrefs.getString(PREF_ACCESS_TOKEN, null);
		String secret = mPrefs.getString(PREF_ACCESS_TOKEN_SECRET, null);

		AccessToken at = new AccessToken(token, secret);
		mTwitter.setOAuthAccessToken(at);

		Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show();
	}

	private void dealWithTwitterResponse(Intent intent) {
		Uri uri = intent.getData();

        try {
            if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
                /* Authorise twitter client with user credentials */
                String oauthVerifier = uri.getQueryParameter("oauth_verifier");
                AccessToken at =
                    mTwitter.getOAuthAccessToken(mReqToken, oauthVerifier);
                mTwitter.setOAuthAccessToken(at);

                /* Save creds to preferences for future use */
                String token = at.getToken();
                String secret = at.getTokenSecret();
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString(PREF_ACCESS_TOKEN, token);
                editor.putString(PREF_ACCESS_TOKEN_SECRET, secret);
                editor.commit();

                Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
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

    private class AuthTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... urls) {

            String authUrl = "";
            try {
                Log.i(TAG, "Request App Authentication");
                mReqToken = mTwitter.getOAuthRequestToken(CALLBACK_URL);
                authUrl = mReqToken.getAuthenticationURL();

            } catch (TwitterException e) {
                e.printStackTrace();
                return null;
            }
            return authUrl;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("")) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(result)));
            }
        }
    }

}
