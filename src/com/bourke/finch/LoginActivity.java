package com.bourke.finch;

import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ActionBar;
import android.content.Context;

public class LoginActivity extends FragmentActivity
        implements ActionBar.OnNavigationListener {

    private static final String TAG = "Finch/LoginActivity";

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        /* Set layout and theme */
        setTheme(HomeActivity.THEME_LIGHT);
        setContentView(R.layout.login);
        getActionBar().hide();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }
}
