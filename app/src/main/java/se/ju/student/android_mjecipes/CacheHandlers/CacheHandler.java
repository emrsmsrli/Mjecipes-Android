package se.ju.student.android_mjecipes.CacheHandlers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

import se.ju.student.android_mjecipes.R;

public abstract class CacheHandler {
    private static final String TAG = "CacheHandler";
    private static long EXPIRE_TIME = 0;
    protected static File cacheDir = null;

    public static void setExpireTime(long newExpireTime, Context c) {
        EXPIRE_TIME = newExpireTime;
        save(c);
    }

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

    public static void clearExternalImageData(Context c) {
        File extdir = c.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(extdir != null)
            deleteRecursive(extdir);
    }

    protected static long unixTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    private static void deleteRecursive(File f) {
        if(f.isDirectory())
            for(File file: f.listFiles())
                deleteRecursive(file);
        else if(f.delete())
            Log.i(TAG, "clearExternalImageData: External image file deleted, name: " + f.getName());
        else
            Log.i(TAG, "clearExternalImageData: External image file deleted, name: " + f.getName());
    }

    private static boolean isExpired(File f) {
        String[] tokens = f.getName().split("-");

        long now = unixTimeStamp();
        long expiration = Long.parseLong(tokens[tokens.length - 1]);

        return now - expiration > EXPIRE_TIME;
    }

    private static void save(Context c) {
        c.getSharedPreferences(c.getString(R.string.shared_preference_key), Context.MODE_PRIVATE)
                .edit()
                .putLong(c.getString(R.string.shared_preference_cache_exp_key), EXPIRE_TIME)
                .apply();
    }

    private static void load(Context c) {
        EXPIRE_TIME = c
                .getSharedPreferences(c.getString(R.string.shared_preference_key), Context.MODE_PRIVATE)
                .getLong(c.getString(R.string.shared_preference_cache_exp_key), 24 * 60 * 60);
    }

    public static synchronized ImageCacheHandler getImageCacheHandler(Context c) {
        if(EXPIRE_TIME == 0)
            load(c);

        return ImageCacheHandler.getInstance(c);
    }

    public static synchronized JSONCacheHandler getJSONJsonCacheHandler(Context c) {
        if(EXPIRE_TIME == 0)
            load(c);

        return JSONCacheHandler.getInstance(c);
    }

}
