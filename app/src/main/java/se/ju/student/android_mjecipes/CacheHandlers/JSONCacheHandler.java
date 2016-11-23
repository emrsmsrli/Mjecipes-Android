package se.ju.student.android_mjecipes.CacheHandlers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Account;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;

public class JSONCacheHandler extends CacheHandler {
    private static final String TAG = "JSONCacheHandler";
    private static final Gson gson = new Gson();
    private static JSONCacheHandler instance = null;

    private JSONCacheHandler(Context c) {
        cacheDir = c.getCacheDir();
    }

    public synchronized <T> void writeToCache(@NonNull T[] data, Class<T> type) {
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
                    Log.w(TAG, "writeToCache: Cache already exists, type: " + type.getSimpleName() + ", name: " + files[0].getName(), null);
                    return null;
                } else
                    f = new File(cacheDir, String.format(Locale.ENGLISH, "%s-%s-%d", type.getSimpleName(), getID(data, type), unixTimeStamp()));

                String dataObj = gson.toJson(data, type);

                try {
                    osw = new OutputStreamWriter(new FileOutputStream(f));
                    osw.write(dataObj);
                    osw.flush();

                    Log.i(TAG, "writeToCache: File written to cache, type: " + type.getSimpleName() + ", name: " + f.getName());
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
    public synchronized <T> T[] readFromCache(@NonNull String[] ids, Class<T> type) {
        ArrayList<T> array = new ArrayList<>();

        for(String id: ids)
            array.add(readFromCache(id, type));

        return (T[]) array.toArray();
    }

    @Nullable
    public synchronized <T> T readFromCache(@NonNull final String id, final Class<T> type) {
        File[] files;
        BufferedReader bf = null;

        files = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(String.format("%s-%s-", type.getSimpleName(), id));
            }
        });

        if(files.length == 0) {
            Log.w(TAG, "readFromCache: Cache does not exist, type: " + type.getSimpleName(), null);
            return null;
        }

        T data = null;

        try {
            bf = new BufferedReader(new FileReader(files[0]));
            data = gson.fromJson(bf.readLine(), type);

            Log.i(TAG, "readFromCache: File read from cache, type: " + type.getSimpleName() + ", name: " + files[0].getName());
        } catch(IOException e) {
            Log.e(TAG, "readFromCache: IO Exception", e);
        } finally {
            try {
                if (bf != null)
                    bf.close();
            } catch(IOException e) {
                Log.e(TAG, "readFromCache: IO Exception", e);
            }
        }

        return data;
    }

    public <T> void clearSingleJSONCache(String id, Class<T> type) {
        final String simpleName = type.getSimpleName();
        final String typeID = id;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = cacheDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith(simpleName + "-" + typeID);
                    }
                });

                for(File f: files)
                    if(f.delete()) Log.i(TAG, "clearAllCaches: Cache file deleted, name: " + f.getName());
                    else           Log.i(TAG, "clearAllCaches: Cache file not deleted, name " + f.getName());
            }
        }).run();
    }

    public <T> void clearAllJSONCachesOfType(Class<T> type) {
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

    static synchronized JSONCacheHandler getInstance(Context c) {
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

}
