package com.bourke.finch;

import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.lazylist.TestAdapter;

public class FinchActivity extends SherlockActivity
        implements ActionBar.OnNavigationListener {

    public static int THEME_DARK = R.style.Theme_Sherlock;
    public static int THEME_LIGHT = R.style.Theme_Sherlock_Light;

    private String[] mLocations;

    private ListView mMainList;
    private LazyAdapter mMainListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /* Set layout and theme */
        setTheme(THEME_LIGHT);
        setContentView(R.layout.main);

        /* Set up actionbar navigation spinner */
        mLocations = getResources().getStringArray(R.array.locations);
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this,
                R.array.locations, R.layout.sherlock_spinner_item);
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
