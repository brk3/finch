package com.bourke.finch.provider;

import android.content.ContentProvider;
import android.content.UriMatcher;

import android.net.Uri;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import android.content.ContentResolver;

public class FinchProvider extends ContentProvider {

    public static final String TAG = "finch/FinchProvider";

    /* The root authority for this provider */
    public static final String AUTHORITY = "com.bourke.finch.provider";

    private static final String BASE_PATH = "screennames";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE =
        ContentResolver.CURSOR_DIR_BASE_TYPE + "/screennames";
    public static final String CONTENT_ITEM_TYPE =
        ContentResolver.CURSOR_ITEM_BASE_TYPE + "/screenname";

    public static final int SCREEN_NAMES = 0;
    public static final int SCREEN_NAME = 1;

    private static final UriMatcher mUriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        mUriMatcher.addURI(AUTHORITY, BASE_PATH, SCREEN_NAMES);
        mUriMatcher.addURI(AUTHORITY, BASE_PATH+"/*", SCREEN_NAME);
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "XXX: onCreate()");
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {

            case SCREEN_NAME:
                return "vnd.android.cursor.item/vnd.finch.sceenname";
            default:
                // any other kind of URL is illegal
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sort) {
        return null;
    }


}
