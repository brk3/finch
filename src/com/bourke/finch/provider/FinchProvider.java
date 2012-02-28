package com.bourke.finch.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;

import android.util.Log;

public class FinchProvider extends ContentProvider {

    public static final String TAG = "finch/FinchProvider";

    /* The root authority for this provider */
    public static final String AUTHORITY = "com.bourke.finch.provider";

    private static final String BASE_PATH = "screenname";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

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
        /* The provider was successfully loaded */
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {

        switch (mUriMatcher.match(uri)) {
            case SCREEN_NAMES:
                return "vnd.android.cursor.dir/vnd.finch.screenname";

            case SCREEN_NAME:
                return "vnd.android.cursor.item/vnd.finch.screenname";

            default:
                // any other kind of URL is illegal
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sort) {
        throw new UnsupportedOperationException();
    }

}
