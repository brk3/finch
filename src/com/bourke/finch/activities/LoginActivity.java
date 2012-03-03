package com.bourke.finch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;

import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;

import android.view.View;

import android.widget.EditText;
import android.widget.Toast;

import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import twitter4j.Twitter;

import twitter4j.TwitterException;

import twitter4j.TwitterFactory;

public class LoginActivity extends FragmentActivity
        implements ActionBar.OnNavigationListener {

    private static final String TAG = "Finch/LoginActivity";

    private Context mContext;

    private SharedPreferences mPrefs;

    private Twitter mTwitter;

	private RequestToken mReqToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        mPrefs = getSharedPreferences("twitterPrefs", MODE_PRIVATE);

        /* Set layout and theme */
        setTheme(FinchApplication.THEME_LIGHT);
        setContentView(R.layout.login);
        getSupportActionBar().hide();

		/* Load the twitter4j helper */
		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(FinchApplication.CONSUMER_KEY,
                FinchApplication.CONSUMER_SECRET);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Uri uri = intent.getData();
        if (uri != null &&
                uri.toString().startsWith(FinchApplication.CALLBACK_URL)) {
            new HandleAuthTask(uri).execute();
        }
	}

    public void loginUser(View view) {
        new RequestAuthTask().execute();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }

    private class RequestAuthTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... urls) {

            String authUrl = "";
            try {
                Log.i(TAG, "Request App Authentication");
                mReqToken = mTwitter.getOAuthRequestToken(
                        FinchApplication.CALLBACK_URL);
                authUrl = mReqToken.getAuthenticationURL();

            } catch (TwitterException e) {
                e.printStackTrace();
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

    private class HandleAuthTask extends AsyncTask<Void, Void, Void> {

        private Uri mUri;
        private AccessToken mAccessToken;

        public HandleAuthTask(Uri uri) {
            mUri = uri;
            mAccessToken = null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                /* Authorise twitter client with user credentials */
                String oauthVerifier = mUri.getQueryParameter(
                        "oauth_verifier");
                mAccessToken = mTwitter.getOAuthAccessToken(mReqToken,
                        oauthVerifier);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (mAccessToken == null) {
                return;
            }

            mTwitter.setOAuthAccessToken(mAccessToken);

            /* Save creds to preferences for future use */
            String token = mAccessToken.getToken();
            String secret = mAccessToken.getTokenSecret();
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(FinchApplication.PREF_ACCESS_TOKEN, token);
            editor.putString(FinchApplication.PREF_ACCESS_TOKEN_SECRET,
                    secret);
            editor.commit();

            Toast.makeText(mContext, "Logged In", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(mContext, FinchActivity.class));
        }
    }
}
