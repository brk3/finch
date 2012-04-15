package com.bourke.finch.lazylist;

import android.app.Activity;

import android.content.Context;

import android.graphics.Bitmap;

import android.widget.ImageView;

import com.bourke.finch.R;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class ImageLoader {

    private MemoryCache memoryCache = new MemoryCache();
    private Map<ImageView, String> imageViews =
        Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private ExecutorService executorService;

    protected Activity mActivity;
    protected Context mContext;
    protected FileCache mFileCache;
    protected final int stub_id = R.drawable.ic_contact_picture;

    public ImageLoader(Activity a) {
        mActivity = a;
        mContext = a.getApplicationContext();
        mFileCache = new FileCache(mContext);
        executorService = Executors.newFixedThreadPool(5);
    }

    public void displayImage(String screenName, ImageView imageView) {
        imageViews.put(imageView, screenName);
        Bitmap bitmap = memoryCache.get(screenName);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            queuePhoto(screenName, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    public abstract Bitmap getBitmap(String entityTag);

    private void queuePhoto(String screenName, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(screenName, imageView);
        executorService.submit(new PhotosLoader(p));
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
        mFileCache.clear();
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
