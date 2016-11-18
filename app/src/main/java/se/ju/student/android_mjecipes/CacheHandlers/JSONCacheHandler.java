package se.ju.student.android_mjecipes.CacheHandlers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Account;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;

public class JSONCacheHandler {
    private static final String TAG = "JSONCacheHandler";
    private static final Gson gson = new Gson();
    private static int EXPIRE_TIME = 24 * 60 * 60; //one day expiration
    private static File cacheDir = null;
    private static JSONCacheHandler instance = null;

    private JSONCacheHandler(Context c) {
        cacheDir = c.getCacheDir();
    }

    public <T> void writeToCache(@NonNull T[] data, Class<T> type) {
        for(T s: data)
            writeToCache(s, type);
    }

    public synchronized <T> void writeToCache(@NonNull final T data, final Class<T> type) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... p) {
                File[] files;
                File f;
                OutputStreamWriter osw = null;

                files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith(String.format("%s-%s-", type.getSimpleName(), getID(data, type)));
                    }
                });

                if(files.length != 0) {
                    if(!expired((files[0]))) {
                        Log.w(TAG, "writeToCache: Cache already exists, type: " + type.getSimpleName(), null);
                        return null;
                    } else {
                        String[] parts = files[0].getName().split("-");
                        files[0].delete();
                        Log.w(TAG, "writeToCache: Cache expired. Writing new one, type: " + type.getSimpleName(), null);
                        f = new File(cacheDir, String.format(Locale.ENGLISH, "%s-%s-%d", parts[0], parts[1], unixTimeStamp()));
                    }
                } else
                    f = new File(cacheDir, String.format(Locale.ENGLISH, "%s-%s-%d", type.getSimpleName(), getID(data, type), unixTimeStamp()));

                String dataObj = gson.toJson(data, type);

                try {
                    osw = new OutputStreamWriter(new FileOutputStream(f));
                    osw.write(dataObj);
                    osw.flush();

                    Log.i(TAG, "writeToCache: File written to cache, type: " + type.getSimpleName());
                } catch(FileNotFoundException e) {
                    Log.e(TAG, "writeToCache: File not found", e);
                } catch(IOException e) {
                    Log.e(TAG, "writeToCache: IO Exception", e);
                } finally {
                    try {
                        if (osw != null)
                            osw.close();
                    } catch(IOException e) {
                        Log.e(TAG, "writeToCache: IO Exception", e);
                    }
                }

                return null;
            }
        }.execute();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T[] readFromCache(@NonNull String[] ids, Class<T> type) {
        ArrayList<T> array = new ArrayList<>();

        for(String id: ids)
            array.add(readFromCache(id, type));

        return (T[]) array.toArray();
    }

    @Nullable
    public synchronized <T> T readFromCache(@NonNull final String id, final Class<T> type) {
        File[] files;
        Scanner s = null;

        files = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(String.format("%s-%s-", type.getSimpleName(), id));
            }
        });

        if(files.length == 0) {
            Log.w(TAG, "readFromCache: Cache does not exist, type: " + type.getSimpleName(), null);
            return null;
        } else if(expired(files[0])) {
            files[0].delete();
            Log.w(TAG, "readFromCache: Cache expired, type: " + type.getSimpleName(), null);
            return null;
        }

        T data = null;

        try {
            s = new Scanner(files[0]);
            data = gson.fromJson(s.nextLine(), type);

            Log.i(TAG, "readFromCache: File read from cache, type: " + type.getSimpleName());
        } catch(IOException e) {
            Log.e(TAG, "readFromCache: IO Exception", e);
        } finally {
            if(s != null)
                s.close();
        }

        return data;
    }

    public static synchronized JSONCacheHandler getInstance(Context c) {
        if(instance == null)
            instance = new JSONCacheHandler(c);

        return instance;
    }

    private <T> String getID(T data, Class<T> type) {
        if(type.equals(Account.class))
            return ((Account) data).id;
        else if(type.equals(Recipe.class))
            return Integer.toString(((Recipe) data).id);
        else if(type.equals(Comment.class))
            return Integer.toString(((Comment) data).id);
        else return "minus1";
    }

    private boolean expired(File f) {
        long expiration = Long.parseLong(f.getName().split("-")[2]);
        return unixTimeStamp() - expiration > EXPIRE_TIME;
    }

    private long unixTimeStamp() {
        return System.currentTimeMillis()/1000;
    }
}
