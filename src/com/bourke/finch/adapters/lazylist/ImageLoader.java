package com.bourke.finch.lazylist;

import android.app.Activity;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.widget.ImageView;

import com.bourke.finch.common.TwitterTask;
import com.bourke.finch.common.FinchTwitterFactory;
import com.bourke.finch.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Map;
import java.util.WeakHashMap;

import twitter4j.ProfileImage;

import twitter4j.Twitter;

import twitter4j.TwitterException;

public class ImageLoader {

    private MemoryCache memoryCache = new MemoryCache();
    private FileCache fileCache;

    private Map<ImageView, String> imageViews =
        Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    private ExecutorService executorService;

    private final int stub_id = R.drawable.ic_contact_picture;

    private Activity mActivity;

    private Context mContext;

    public ImageLoader(Activity a) {
        mActivity = a;
        mContext = a.getApplicationContext();
        fileCache = new FileCache(mContext);
        executorService = Executors.newFixedThreadPool(5);
    }

    public void displayImage(String screenName, ImageView imageView) {
        imageViews.put(imageView, screenName);
        Bitmap bitmap = memoryCache.get(screenName);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(screenName, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(String screenName, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(screenName, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String screenName) {
        File f = fileCache.getFile(screenName);

        /* From sd cache. Original lazylist code uses a custom decodeFile
         * method here, but ours comes prescaled courtesy of twitter */
        //Bitmap bitmap = BitmapFactory.decodeFile(f.toString());
        Bitmap bitmap = null;

        //if (bitmap == null) {
            /* From web */
            try {
                Twitter twitter = FinchTwitterFactory.getInstance(mContext)
                    .getTwitter();
                ProfileImage p = twitter.getProfileImage(
                        screenName, ProfileImage.BIGGER);

                URL imageUrl = new URL(p.getURL());
                HttpURLConnection conn =
                    (HttpURLConnection)imageUrl.openConnection();
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
        //}

        return bitmap;
    }

    private boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.screenName)) {
            return true;
        }
        return false;
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

    /* Task for the queue */
    private class PhotoToLoad {
        public String screenName;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i){
            screenName = u;
            imageView = i;
        }
    }

    private class PhotosLoader implements Runnable {
        private PhotoToLoad photoToLoad;

        public PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if(imageViewReused(photoToLoad)) {
                return;
            }
            Bitmap bmp = getBitmap(photoToLoad.screenName);
            memoryCache.put(photoToLoad.screenName, bmp);
            if(imageViewReused(photoToLoad)) {
                return;
            }
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    /* Used to display bitmap in the UI thread */
    private class BitmapDisplayer implements Runnable {
        private Bitmap bitmap;
        private PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if(imageViewReused(photoToLoad)) {
                return;
            }
            if(bitmap!=null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
            } else {
                photoToLoad.imageView.setImageResource(stub_id);
            }
        }
    }

}
