package se.ju.student.android_mjecipes.CacheHandlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import se.ju.student.android_mjecipes.R;

public abstract class CacheHandler {
    public static final long EXPIRE_TIME_HIGH = 24 * 60 * 60; //24h
    public static final long EXPIRE_TIME_NORMAL = 12 * 60 * 60; //12h
    public static final long EXPIRE_TIME_LOW = 3 * 60 * 60; //3h
    public static final long CACHE_LIMIT_HIGH = 2 << 24; //32mb
    public static final long CACHE_LIMIT_NORMAL = 2 << 23; //16mb
    public static final long CACHE_LIMIT_LOW = 2 << 21; //4mb
    public static final int IMAGE_QUALITY_REAL = 100;
    public static final int IMAGE_QUALITY_HIGH = 75;
    public static final int IMAGE_QUALITY_NORMAL = 50;
    public static final int IMAGE_QUALITY_LOW = 25;

    private static final String TAG = "CacheHandler";
    private static long EXPIRE_TIME = 0;
    private static long CACHE_SIZE_LIMIT = 0;
    private static long CACHE_SIZE_TOTAL = 0;
    protected static int IMAGE_CACHE_QUALITY = 0;
    protected static File cacheDir = null;

    public static void setExpireTime(long newExpireTime, Context c) {
        EXPIRE_TIME = newExpireTime;
        save(c);
    }

    public static void setCacheSizeLimit(long newCacheSizeLimit, Context c) {
        CACHE_SIZE_LIMIT = newCacheSizeLimit;
        save(c);
    }

    public static void setImageCacheQuality(int newImageCacheQualityValue, Context c) {
        IMAGE_CACHE_QUALITY = newImageCacheQualityValue;
        save(c);
    }

    public static long getExpireTime() {
        return EXPIRE_TIME;
    }

    public static long getCacheSizeLimit() {
        return CACHE_SIZE_LIMIT;
    }

    public static int getImageCacheQuality() {
        return IMAGE_CACHE_QUALITY;
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

    protected static void addCacheSize(long fileSize) {
        CACHE_SIZE_TOTAL += fileSize;
        deleteIfLimitReached();
    }

    private static void deleteIfLimitReached() {
        if(CACHE_SIZE_TOTAL >= CACHE_SIZE_LIMIT)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File[] files = getOldestCaches();
                    for(File f: files) {
                        if(f != null) {
                            long size = f.length();
                            if(f.delete()) {
                                Log.i(TAG, "deleteIfLimitReached: Cache limit reached, file deleted, name: " + f.getName());
                                CACHE_SIZE_TOTAL -= size;
                            } else
                                Log.i(TAG, "deleteIfLimitReached: Cache limit reached, but file not deleted, name " + f.getName());
                        }
                    }
                }
            }).run();
    }

    @NonNull
    private static File[] getOldestCaches() {
        File[] files = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith("image") || filename.startsWith("Recipe") ||
                        filename.startsWith("Account") || filename.startsWith("Comment");
            }
        });

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                long creationTimeL = getCreationTime(lhs);
                long creationTimeR = getCreationTime(rhs);
                if(creationTimeL > creationTimeR)
                    return 1;
                else if(creationTimeL < creationTimeR)
                    return -1;
                return 0;
            }
        });

        return Arrays.copyOfRange(files, 0, 10);
    }

    private static void getCacheSize(final Context c) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long totalSize = 0;
                File[] files = c.getCacheDir().listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith("image") || filename.startsWith("Recipe") ||
                                filename.startsWith("Account") || filename.startsWith("Comment");
                    }
                });

                for(File f: files)
                    totalSize += f.length();

                CACHE_SIZE_TOTAL = totalSize;
            }
        }).run();
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
        long now = unixTimeStamp();
        long creationTime = getCreationTime(f);

        return now - creationTime > EXPIRE_TIME;
    }

    private static long getCreationTime(File f) {
        String[] parts = f.getName().split("-");
        return Long.parseLong(parts[parts.length - 1]);
    }

    private static void save(Context c) {
        c.getSharedPreferences(c.getString(R.string.shared_preference_key), Context.MODE_PRIVATE)
                .edit()
                .putLong(c.getString(R.string.shared_preference_cache_exp_key), EXPIRE_TIME)
                .putLong(c.getString(R.string.shared_preference_cache_limit_key), CACHE_SIZE_LIMIT)
                .putInt(c.getString(R.string.shared_preference_cache_quality_key), IMAGE_CACHE_QUALITY)
                .apply();
    }

    private static void load(Context c) {
        SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preference_key), Context.MODE_PRIVATE);
        EXPIRE_TIME = sp.getLong(c.getString(R.string.shared_preference_cache_exp_key), EXPIRE_TIME_NORMAL);
        CACHE_SIZE_LIMIT = sp.getLong(c.getString(R.string.shared_preference_cache_limit_key), CACHE_LIMIT_NORMAL);
        IMAGE_CACHE_QUALITY = sp.getInt(c.getString(R.string.shared_preference_cache_quality_key), IMAGE_QUALITY_NORMAL);

        getCacheSize(c);
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
