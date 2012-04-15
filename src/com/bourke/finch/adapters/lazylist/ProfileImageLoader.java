package com.bourke.finch.lazylist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bourke.finch.common.FinchTwitterFactory;

import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import twitter4j.ProfileImage;

import twitter4j.Twitter;

import twitter4j.TwitterException;
import android.app.Activity;

public class ProfileImageLoader extends ImageLoader {

    public ProfileImageLoader(Activity a) {
        super(a);
    }

    @Override
    public Bitmap getBitmap(String screenName) {
        File f = mFileCache.getFile(screenName);

        /* From sd cache */
        //Bitmap bitmap = Utils.decodeFile(f);
        Bitmap bitmap = BitmapFactory.decodeFile(f.toString());

        if (bitmap == null) {
            /* From web */
            try {
                Twitter twitter = FinchTwitterFactory.getInstance(mContext)
                    .getTwitter();
                ProfileImage p = twitter.getProfileImage(screenName,
                        ProfileImage.BIGGER);
                URL imageUrl = new URL(p.getURL());
                HttpURLConnection conn = (HttpURLConnection)imageUrl
                    .openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(f);
                Utils.CopyStream(is, os);
                os.close();
                bitmap = BitmapFactory.decodeFile(f.toString());
            } catch (TwitterException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
