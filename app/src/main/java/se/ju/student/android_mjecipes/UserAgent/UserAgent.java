package se.ju.student.android_mjecipes.UserAgent;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.R;

public class UserAgent {
    private static UserAgent instance = null;
    private static SharedPreferences sharedPreferences = null;
    private static Resources resources = null;
    private static boolean loggedIn = false;
    private static String userID = null;
    private static String username = null;

    private UserAgent(Context c) {
        resources = c.getResources();
        sharedPreferences = c.getSharedPreferences(resources.getString(R.string.shared_preference_key), Context.MODE_PRIVATE);
        load();
    }

    public boolean login(final String userName, final String password) {
        if(loggedIn) return true;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                JWToken token = Handler.getTokenHandler().getToken(userName, password);

                if(token != null) {
                    userID = token.getUserID();
                    username = userName;
                    loggedIn = true;
                }

                save();
            }
        });

        try {
            t.start();
            t.join();
        } catch(InterruptedException e) {
            return false;
        }

        return isLoggedIn();
    }

    public boolean logout() {
        if(!loggedIn) return true;

        userID = null;
        username = null;
        loggedIn = false;
        save();

        return !isLoggedIn();
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    private void save() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(resources.getString(R.string.shared_preference_logged_in_key), loggedIn);
        editor.putString(resources.getString(R.string.shared_preference_userid_key), userID);
        editor.putString(resources.getString(R.string.shared_preference_username_key), username);

        editor.apply();
    }

    private void load() {
        loggedIn = sharedPreferences.getBoolean(resources.getString(R.string.shared_preference_logged_in_key), false);
        userID = sharedPreferences.getString(resources.getString(R.string.shared_preference_userid_key), null);
        username = sharedPreferences.getString(resources.getString(R.string.shared_preference_username_key), null);
    }

    public static String getUserID() {
        return userID;
    }

    public static String getUsername() {
        return username;
    }

    public static UserAgent getInstance(Context c) {
        if(instance == null)
            instance = new UserAgent(c);

        return instance;
    }

}
