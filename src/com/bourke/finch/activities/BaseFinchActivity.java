package com.bourke.finch;

import android.os.Bundle;

import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.Window;

import android.widget.ArrayAdapter;

public abstract class BaseFinchActivity extends FragmentActivity
        implements ActionBar.OnNavigationListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Set layout and theme */
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(FinchApplication.THEME_LIGHT);

        /* Set up actionbar navigation spinner */
        ArrayAdapter<CharSequence> list =
            ArrayAdapter.createFromResource(this, R.array.locations,
                R.layout.spinner_title);
        list.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        getSupportActionBar().setNavigationMode(
                ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Write")
            .setIcon(R.drawable.ic_action_edit)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add("Refresh")
            .setIcon(R.drawable.ic_action_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add("Search")
            .setIcon(R.drawable.ic_action_search)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return true;
    }
}
