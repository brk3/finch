package com.bourke.finch.lazylist;

import android.app.Activity;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.widget.ImageView;

import com.bourke.finch.FinchApplication;
import com.bourke.finch.R;
import com.bourke.finch.TwitterTask;

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

import twitter4j.Twitter;

public class ImageLoader {

    private MemoryCache memoryCache = new MemoryCache();
    private FileCache fileCache;

    private Map<ImageView, String> imageViews =
        Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private Map<ImageView, String> screenNames =
        Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    private ExecutorService executorService;

    private final int stub_id = R.drawable.ic_contact_picture;

    private Activity mActivity;

    public ImageLoader(Activity a) {
        mActivity = a;
        fileCache = new FileCache(mActivity.getApplicationContext());
        executorService = Executors.newFixedThreadPool(5);
    }

    public void displayImage(String screenName, ImageView imageView) {
        screenNames.put(imageView, screenName);
        TwitterTask.Payload getProfileImageParams = new TwitterTask.Payload(
                TwitterTask.GET_PROFILE_IMAGE, new Object[] {
                    this, screenName, imageView});
        Twitter twitter = ((FinchApplication)mActivity.getApplication())
            .getTwitter();
        new TwitterTask(getProfileImageParams, twitter).execute();
    }

    public void _displayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);

        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url);

        /* From sd cache */
        //Bitmap b = decodeFile(f);
        Bitmap b = BitmapFactory.decodeFile(f);
        if (b != null)
            return b;

        /* From web */
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn =
                (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Exception ex){
           ex.printStackTrace();
           return null;
        }
    }

    /* Decodes image and scales it to reduce memory consumption */
    private Bitmap decodeFile(File f) {
        try {
            /* Decode image size */
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            /* Find the correct scale value. It should be the power of 2. */
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp=o.outHeight;
            int scale = 1;

            while (true) {
                if (width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            /* Decode with inSampleSize */
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(
                    new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }

        return null;
    }

    private boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag==null || !tag.equals(photoToLoad.url)) {
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
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i){
            url = u;
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
            Bitmap bmp = getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
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
