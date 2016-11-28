package se.ju.student.android_mjecipes.MjepicesAPIHandler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Account;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;

public class AccountHandler extends Handler {
    private static AccountHandler instance;
    private static final String TAG = "AccountHandler";

    private AccountHandler() {
        super();
    }

    @Nullable
    public Account getAccount(@NonNull String id) {
        BufferedReader br = null;
        HttpURLConnection connection = null;
        Account account = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + ACCOUNTS_URL + id).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "getAccount: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_OK:
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    account = gson.fromJson(br.readLine(), Account.class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getAccount: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO Exception", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "doInBackground: IO Exception", e);
            }

            if(connection != null)
                connection.disconnect();
        }

        return account;
    }

    public boolean postAccount(@NonNull Account account) {
        String passwordstr = "password/";
        HttpURLConnection connection = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + ACCOUNTS_URL + passwordstr).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            pw = new PrintWriter(connection.getOutputStream());
            pw.print(gson.toJson(account, Account.class));
            pw.flush();

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                    errors = gson.fromJson(br.readLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_CREATED;
                    Log.i(TAG, "postAccount: HTTP Created");
                    errors.error = getCreatedId(connection.getHeaderField("Location"));
                    break;
                default:
                    Log.i(TAG, "postAccount: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "postAccount: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "postAccount: IO Except", e);
         } finally {
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "postAccount: IO Except", e);
            }

            if(pw != null)
                pw.close();
            if(connection != null)
                connection.disconnect();
        }

        return toReturn;
    }

    public boolean patchAccount(@NonNull String id, @NonNull Account a, @NonNull JWToken token) {
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + ACCOUNTS_URL + id).openConnection();
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
            connection.setRequestProperty("Content-Type", "application/json");

            pw = new PrintWriter(connection.getOutputStream());
            pw.print(gson.toJson(a, Account.class));
            pw.flush();

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "patchAccount: HTTP Unauthorized");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "patchAccount: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_NO_CONTENT;
                    break;
                default:
                    Log.i(TAG, "patchAccount: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }
        } catch(MalformedURLException e) {
            Log.e(TAG, "patchAccount: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "patchAccount: IO Exception", e);
        } finally {
            if(pw != null)
                pw.close();
            if(connection != null)
                connection.disconnect();
        }

        return toReturn;
    }

    public boolean deleteAccount(@NonNull String id, @NonNull JWToken token) {
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + ACCOUNTS_URL + id).openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "deleteAccount: HTTP Unauthorized");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "deleteAccount: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_NO_CONTENT;
                    break;
                default:
                    Log.i(TAG, "deleteAccount: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "deleteAccount: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "deleteAccount: IO Exception", e);
        } finally {
            if(connection != null)
                connection.disconnect();
        }

        return toReturn;
    }

    @Nullable
    public Recipe[] getRecipes(@NonNull String id) {
        String r = "/recipes";
        BufferedReader br = null;
        HttpURLConnection connection = null;
        Recipe[] recipes = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + ACCOUNTS_URL + id + r).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "getRecipes: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_OK:
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    recipes = gson.fromJson(br.readLine(), Recipe[].class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getRecipes: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "getRecipes: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "getRecipes: IO Exception", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "getRecipes: IO Exception", e);
            }

            if(connection != null)
                connection.disconnect();
        }

        return recipes;
    }

    @Nullable
    public Comment[] getComments(@NonNull String id) {
        String c = "/comments";
        BufferedReader br = null;
        HttpURLConnection connection = null;
        Comment[] comments = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + ACCOUNTS_URL + id + c).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "getComments: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_OK:
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    comments = gson.fromJson(br.readLine(), Comment[].class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getComments: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "getComments: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "getComments: IO Exception", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "getComments: IO Exception", e);
            }

            if(connection != null)
                connection.disconnect();
        }

        return comments;
    }

    public boolean putFavorites(@NonNull String id, @NonNull int[] recipeids, @NonNull JWToken token) {
        Recipe[] recipes = new Recipe[recipeids.length];

        for(int i = 0; i < recipeids.length; ++i) {
            recipes[i] = new Recipe();
            recipes[i].id = recipeids[i];
        }

        return putFavorites(id, recipes, token);
    }

    public boolean putFavorites(@NonNull String id, @NonNull Recipe[] recipes, @NonNull JWToken token) {
        String favoritesstr = "/favorites";
        BufferedReader br = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + ACCOUNTS_URL + id + favoritesstr).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            pw = new PrintWriter(connection.getOutputStream());
            pw.print(gson.toJson(recipes, Recipe[].class));
            pw.flush();

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "putFavorites: HTTP Unauthorized");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "putFavorites: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    Log.i(TAG, "putFavorites: HTTP Bad Request");
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                    errors = gson.fromJson(br.readLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_NO_CONTENT;
                    break;
                default:
                    Log.i(TAG, "putFavorites: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "putFavorites: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "putFavorites: IO Exception", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "putFavorites: IO Exception", e);
            }

            if(pw != null)
                pw.close();
            if(connection != null)
                connection.disconnect();
        }

        return toReturn;
    }

    @Nullable
    public Recipe[] getFavorites(@NonNull String id, @NonNull JWToken token) {
        String f = "/favorites";
        BufferedReader br = null;
        HttpURLConnection connection = null;
        Recipe[] recipes = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + ACCOUNTS_URL + id + f).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "getFavorites: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "getFavorites: HTTP Not Found or Unauth");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_OK:
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    recipes = gson.fromJson(br.readLine(), Recipe[].class);

                    for(int i = 0; i < recipes.length; ++i)
                        recipes[i] = getRecipeHandler().getRecipe(recipes[i].id);

                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getFavorites: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "getFavorites: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "getFavorites: IO Exception", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "getFavorites: IO Exception", e);
            }

            if(connection != null)
                connection.disconnect();
        }

        return recipes;
    }

    static AccountHandler getInstance() {
        if(instance == null)
            instance = new AccountHandler();

        return instance;
    }

}
