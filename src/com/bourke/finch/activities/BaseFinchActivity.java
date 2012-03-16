package com.bourke.finch;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.Shader.TileMode;

import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;

import android.util.Log;

import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public abstract class BaseFinchActivity extends SherlockFragmentActivity
        implements ActionBar.OnNavigationListener {

    private static final String TAG = "finch/BaseFinchActivity";

    private MenuItem mMenuItemProgress;
    private MenuItem mMenuItemRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Workaround for http://b.android.com/15340 from
         * http://stackoverflow.com/a/5852198/132047
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(
                    R.drawable.bg_light_grey_stripe);
            bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setBackgroundDrawable(bg);

            /*
            BitmapDrawable bgSplit = (BitmapDrawable)getResources()
                .getDrawable(R.drawable.bg_striped_split_img);
            bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
            */
        }
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
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);

        mMenuItemProgress = menu.findItem(R.id.menu_progress);
        mMenuItemRefresh = menu.findItem(R.id.menu_refresh);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                //Intent intent = new Intent(this, HomeActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
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

}
