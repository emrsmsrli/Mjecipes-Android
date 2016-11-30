package se.ju.student.android_mjecipes.UserAgent;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.R;

public class UserAgent {

    public interface LoginListener {
        void onLogin(boolean loggedIn);
    }

    public interface FavoriteListener {
        void onFavoritePosted(boolean posted);
    }

    private static UserAgent instance = null;
    private static SharedPreferences sharedPreferences = null;
    private static Resources resources = null;
    private static boolean loggedIn = false;
    private static String userID = null;
    private static String username = null;
    private static String password = null;
    private static ArrayList<Integer> favoriteRecipeIDs = null;

    private UserAgent(Context c) {
        resources = c.getResources();
        sharedPreferences = c.getSharedPreferences(resources.getString(R.string.shared_preference_key), Context.MODE_PRIVATE);
        load();
    }

    public void login(final String userName, final String passWord, @NonNull final LoginListener listener) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                JWToken token = Handler.getTokenHandler().getToken(userName, passWord);

                if(token != null) {
                    userID = token.getUserID();
                    username = userName;
                    password = passWord;
                    loggedIn = true;
                    getFavorites();
                }

                save();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                listener.onLogin(isLoggedIn());
            }

        }.execute();
    }

    public void logout() {
        userID = null;
        username = null;
        password = null;
        loggedIn = false;
        favoriteRecipeIDs = null;
        save();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    private void save() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(resources.getString(R.string.shared_preference_logged_in_key), loggedIn);
        editor.putString(resources.getString(R.string.shared_preference_userid_key), userID);
        editor.putString(resources.getString(R.string.shared_preference_username_key), username);
        editor.putString(resources.getString(R.string.shared_preference_password_key), password);

        editor.apply();
    }

    private void load() {
        loggedIn = sharedPreferences.getBoolean(resources.getString(R.string.shared_preference_logged_in_key), false);
        userID = sharedPreferences.getString(resources.getString(R.string.shared_preference_userid_key), null);
        username = sharedPreferences.getString(resources.getString(R.string.shared_preference_username_key), null);
        password = sharedPreferences.getString(resources.getString(R.string.shared_preference_password_key), null);
    }

    private void getFavorites() {
        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... params) {
                JWToken token = Handler.getTokenHandler().getToken(
                        getUsername(),
                        getPassword()
                );

                if(token != null)
                    return Handler.getAccountHandler().getFavorites(userID, token);
                return null;
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {
                if(recipes != null && recipes.length > 0) {
                    favoriteRecipeIDs = new ArrayList<>();
                    for(Recipe r: recipes)
                        favoriteRecipeIDs.add(r.id);
                }
            }

        }.execute();
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasFavorite(int recipeId) {
        return favoriteRecipeIDs != null && favoriteRecipeIDs.contains(recipeId);
    }

    public void postFavorite(int recipeId, @NonNull final FavoriteListener listener) {
        new AsyncTask<Integer, Void, Boolean>() {
            private int rid;

            @Override
            protected Boolean doInBackground(Integer... params) {
                rid = params[0];
                JWToken token = Handler.getTokenHandler().getToken(
                        getUsername(),
                        getPassword()
                );

                return token != null && Handler.getAccountHandler().putFavorites(
                        getUserID(),
                        new int[]{rid},
                        token
                );
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result)
                    favoriteRecipeIDs.add(rid);

                listener.onFavoritePosted(result);
            }

        }.execute(recipeId);
    }

    public static UserAgent getInstance(Context c) {
        if(instance == null)
            instance = new UserAgent(c);

        return instance;
    }

}
