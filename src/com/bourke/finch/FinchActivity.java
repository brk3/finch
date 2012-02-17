package com.bourke.finch;

import android.os.Bundle;

import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bourke.finch.lazylist.LazyAdapter;
import com.bourke.finch.lazylist.TestAdapter;

import twitter4j.auth.AccessToken;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class FinchActivity extends FragmentActivity
        implements ActionBar.OnNavigationListener {

    private static final String CONSUMER_KEY = "7QIjyQd3cA8c8jn80tRtqw";
    private static final String CONSUMER_SECRET =
        "yREZDaGBZfIGnlNGce9m80jRUKbnkYhZGT7XZkFZqg";

    public static int THEME_DARK = R.style.Theme_Finch;
    public static int THEME_LIGHT = R.style.Theme_Finch_Light;

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

        /* Check for saved log in details */
        //checkForSavedLogin();

        /* Set consumer and provider on teh Application service */
        //getConsumerProvider();

        /* Set actionbar subtitle to user's username */
        getSupportActionBar().setSubtitle("@brk3");
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
