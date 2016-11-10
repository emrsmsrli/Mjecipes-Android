package se.ju.student.android_mjecipes.MjepicesAPIHandler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
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
        Scanner s = null;
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
                    s = new Scanner(connection.getInputStream());
                    account = gson.fromJson(s.nextLine(), Account.class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getAccount: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e("ACCOUNTHANDLER", "doInBackground: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e("ACCOUNTHANDLER", "doInBackground: IO_ERROR", e);
        } finally {
            if(s != null)
                s.close();
            if(connection != null)
                connection.disconnect();
        }

        return account;
    }

    public boolean postAccount(@NonNull Account account) {
        String passwordstr = "password/";
        HttpURLConnection connection = null;
        Scanner s = null;
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
                    s = new Scanner(connection.getErrorStream());
                    errors = gson.fromJson(s.nextLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_CREATED;
                    break;
                default:
                    Log.i(TAG, "postAccount: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "postAccount: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e(TAG, "postAccount: IO_EXCEPTION", e);
         } finally {
            if(s != null)
                s.close();
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
            Log.e(TAG, "patchAccount: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "patchAccount: IO_EXCEPTION", e);
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
            Log.e(TAG, "deleteAccount: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "deleteAccount: IO_EXCEPTION", e);
        } finally {
            if(connection != null)
                connection.disconnect();
        }

        return toReturn;
    }

    @Nullable
    public Recipe[] getRecipes(@NonNull String id) {
        String r = "/recipes";
        Scanner s = null;
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
                    s = new Scanner(connection.getInputStream());
                    recipes = gson.fromJson(s.nextLine(), Recipe[].class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getRecipes: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e("ACCOUNTHANDLER", "getRecipes: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e("ACCOUNTHANDLER", "getRecipes: IO_ERROR", e);
        } finally {
            if(s != null)
                s.close();
            if(connection != null)
                connection.disconnect();
        }

        return recipes;
    }

    @Nullable
    public Comment[] getComments(@NonNull String id) {
        String c = "/comments";
        Scanner s = null;
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
                    s = new Scanner(connection.getInputStream());
                    comments = gson.fromJson(s.nextLine(), Comment[].class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getComments: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "getComments: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e(TAG, "getComments: IO_ERROR", e);
        } finally {
            if(s != null)
                s.close();
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
        Scanner s = null;
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
                    s = new Scanner(connection.getErrorStream());
                    errors = gson.fromJson(s.nextLine(), Errors.class);
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
            Log.e(TAG, "putFavorites: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "putFavorites: IO_EXCEPTION", e);
        } finally {
            if(s != null)
                s.close();
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
        Scanner s = null;
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
                    s = new Scanner(connection.getInputStream());
                    recipes = gson.fromJson(s.nextLine(), Recipe[].class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getFavorites: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "getFavorites: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "getFavorites: IO_ERROR", e);
        } finally {
            if(s != null)
                s.close();
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
