package se.ju.student.android_mjecipes.CacheHandlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NoCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;

public class ImageCacheHandler {
    private static final String TAG = "ImageCacheHandler";
    private static int EXPIRE_TIME = 24 * 60 * 60; //one day expiration
    private static File cacheDir = null;
    private static ImageCacheHandler instance = null;
    private static RequestQueue rq = null;

    private ImageCacheHandler(Context c) {
        cacheDir = c.getCacheDir();
        Network network = new BasicNetwork(new HurlStack());
        rq = new RequestQueue(new NoCache(), network);
        rq.start();
    }

    public RequestQueue getRequestQueue() {
        return rq;
    }

    public synchronized void addRequest(@NonNull ImageRequest request) {
        getRequestQueue().add(request);
    }

    public synchronized void writeToCache(@NonNull final String url, @NonNull final Bitmap b) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... p) {
                File[] files;
                File f;
                FileOutputStream fos = null;
                final String fname = getFileName(url);

                files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith(String.format("image-%s-", fname));
                    }
                });

                if(files.length != 0) {
                    if(!expired(files[0])) {
                        Log.w(TAG, "writeToCache: Cache already exists, type: Image", null);
                        return null;
                    } else {
                        files[0].delete();
                        Log.w(TAG, "writeToCache: Cache expired. Writing new one, type: Image", null);
                        f = new File(cacheDir, String.format(Locale.ENGLISH, "image-%s-%d", fname, unixTimeStamp()));
                    }
                } else
                    f = new File(cacheDir, String.format(Locale.ENGLISH, "image-%s-%d", fname, unixTimeStamp()));

                try {
                    fos = new FileOutputStream(f);
                    if(b.compress(Bitmap.CompressFormat.PNG, 75, fos)) {
                        fos.flush();
                        Log.i(TAG, "writeToCache: File written to cache, type: Image");
                    }
                } catch(IOException e) {
                    Log.e(TAG, "writeToCache: IO Exception", e);
                } finally {
                    try {
                        if(fos != null)
                            fos.close();
                    } catch(IOException e) {
                        Log.e(TAG, "writeToCache: IO Exception", e);
                    }
                }

                return null;
            }
        }.execute();
    }

    @Nullable
    public synchronized Bitmap readFromCache(@NonNull final String url) {
        File[] files;
        FileInputStream fis = null;
        Bitmap bitmap = null;

        files = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(String.format("image-%s-", getFileName(url)));
            }
        });

        if(files.length == 0) {
            Log.w(TAG, "readFromCache: Cache does not exist, type: Image", null);
            return null;
        } else if(expired(files[0])) {
            files[0].delete();
            Log.w(TAG, "readFromCache: Cache expired, type: Image", null);
            return null;
        }

        try {
            fis = new FileInputStream(files[0]);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(files[0]));
            if(bitmap != null)
                Log.i(TAG, "readFromCache: File read from cache, type: Image");
        } catch(IOException e) {
            Log.e(TAG, "readFromCache: IO Exception", e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch(IOException e) {
                Log.e(TAG, "readFromCache: IO Exception", e);
            }
        }

        return bitmap;
    }

    public synchronized static ImageCacheHandler getInstance(@NonNull Context c) {
        if(instance == null)
            instance = new ImageCacheHandler(c.getApplicationContext());

        return instance;
    }

    private String getFileName(String url) {
        String[] tokens = url.split("/");
        return tokens[tokens.length - 1];
    }

    private boolean expired(File f) {
        long expiration = Long.parseLong(f.getName().split("-")[3]);
        return unixTimeStamp() - expiration > EXPIRE_TIME;
    }

    private long unixTimeStamp() {
        return System.currentTimeMillis()/1000;
    }

}
