package se.ju.student.android_mjecipes.CacheHandlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class ImageCacheHandler extends CacheHandler {
    private static final String TAG = "ImageCacheHandler";
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

    public synchronized void downloadImage(@NonNull ImageRequest request) {
        rq.add(request);
    }

    public synchronized void writeToCache(@NonNull final String url, @NonNull final Bitmap b) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                Log.w(TAG, "writeToCache: Cache already exists, type: Image, name: " + files[0].getName(), null);
                return;
            } else
                f = new File(cacheDir, String.format(Locale.ENGLISH, "image-%s-%d", fname, unixTimeStamp()));

            try {
                fos = new FileOutputStream(f);
                if(b.compress(Bitmap.CompressFormat.JPEG, IMAGE_CACHE_QUALITY, fos)) {
                    fos.flush();
                    addCacheSize(f.length());
                    Log.i(TAG, "writeToCache: File written to cache, type: Image, name: " + f.getName() + " quality: " + IMAGE_CACHE_QUALITY + "%");
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
            }
        }).run();
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
        }

        try {
            fis = new FileInputStream(files[0]);
            bitmap = BitmapFactory.decodeStream(fis);
            if(bitmap != null) Log.i(TAG, "readFromCache: File read from cache, type: Image, name: " + files[0].getName());
            else               Log.i(TAG, "readFromCache: File not read from cache, type: Image, name: " + files[0].getName());
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

    public void clearSingleImageCache(String url) {
        final String name = getFileName(url);
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith("image-" + name);
                    }
                });

                for(File f: files)
                    if(f.delete()) Log.i(TAG, "clearSingleImageCache: Cache file deleted, name: " + f.getName());
                    else           Log.i(TAG, "clearSingleImageCache: Cache file not deleted, name " + f.getName());
            }
        }).run();
    }

    public void clearAllImageCaches() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith("image-");
                    }
                });

                for(File f: files)
                    if(f.delete()) Log.i(TAG, "clearAllImageCaches: Cache file deleted, name: " + f.getName());
                    else           Log.i(TAG, "clearAllImageCaches: Cache file not deleted, name " + f.getName());
            }
        }).run();
    }

    synchronized static ImageCacheHandler getInstance(@NonNull Context c) {
        if(instance == null)
            instance = new ImageCacheHandler(c.getApplicationContext());

        return instance;
    }

    private String getFileName(String url) {
        String[] tokens = url.split("/");
        return tokens[tokens.length - 1];
    }

}
