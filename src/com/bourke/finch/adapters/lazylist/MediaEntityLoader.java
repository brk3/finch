package com.bourke.finch.lazylist;

import android.app.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

public class MediaEntityLoader extends ImageLoader {

    public MediaEntityLoader(Activity a) {
        super(a);
    }

    @Override
    public Bitmap getBitmap(String requestedURL) {
        File f = mFileCache.getFile(requestedURL);

        /* From sd cache */
        //Bitmap bitmap = Utils.decodeFile(f);
        Bitmap bitmap = BitmapFactory.decodeFile(f.toString());

        if (bitmap == null) {
            /* From web */
            try {
                URL imageUrl = new URL(requestedURL+":iphone");
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
