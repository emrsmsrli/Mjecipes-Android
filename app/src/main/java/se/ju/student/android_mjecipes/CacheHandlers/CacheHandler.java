package se.ju.student.android_mjecipes.CacheHandlers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

public abstract class CacheHandler {
    private static final String TAG = "CacheHandler";
    protected static File cacheDir = null;

    /*
     * TODO implement this in seperate handlers
     *
    public static <T> void clearSingleCache(Context c, Class<T> type, String id) {
        final File cacheDir = c.getCacheDir();
        final String simpleName = type.getSimpleName();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith(simpleName);
                    }
                });

                for(File f: files)
                    if(f.delete()) Log.i(TAG, "clearAllCaches: Cache file deleted, name: " + f.getName());
                    else           Log.i(TAG, "clearAllCaches: Cache file not deleted, name " + f.getName());
            }
        }).run();
    }

    public static <T> void clearCachesOf(Context c, Class<T> type) {
        final File cacheDir = c.getCacheDir();
        final String simpleName = type.getSimpleName();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith(simpleName);
                    }
                });

                for(File f: files)
                    if(f.delete()) Log.i(TAG, "clearAllCaches: Cache file deleted, name: " + f.getName());
                    else           Log.i(TAG, "clearAllCaches: Cache file not deleted, name " + f.getName());
            }
        }).run();
    } */

    public static void clearAllCaches(Context c) {
        final File cacheDir = c.getCacheDir();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith("image") || filename.startsWith("Recipe") ||
                                filename.startsWith("Account") || filename.startsWith("Comment");
                    }
                });

                for(File f: files)
                    if(f.delete()) Log.i(TAG, "clearAllCaches: Cache file deleted, name: " + f.getName());
                    else           Log.i(TAG, "clearAllCaches: Cache file not deleted, name " + f.getName());
            }
        }).run();
    }

    public static void clearExpiredCaches(Context c) {
        final File cacheDir = c.getCacheDir();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith("image") || filename.startsWith("Recipe") ||
                                filename.startsWith("Account") || filename.startsWith("Comment");
                    }
                });

                for(File f: files)
                    if(isExpired(f)) {
                        if(f.delete()) Log.i(TAG, "clearExpiredCaches: Expired file deleted, name: " + f.getName());
                        else           Log.i(TAG, "clearExpiredCaches: Expired file not deleted, name " + f.getName());
                    }
            }
        }).run();
    }

    protected static long unixTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    private static boolean isExpired(File f) {
        String[] tokens = f.getName().split("-");

        long now = unixTimeStamp();
        long expiration = Long.parseLong(tokens[tokens.length - 1]);
        long expireTime = 24 * 60 * 60; //one day expiration

        return now - expiration > expireTime;
    }

    public static synchronized ImageCacheHandler getImageCacheHandler(Context c) {
        return ImageCacheHandler.getInstance(c);
    }

    public static synchronized JSONCacheHandler getJSONJsonCacheHandler(Context c) {
        return JSONCacheHandler.getInstance(c);
    }

}
