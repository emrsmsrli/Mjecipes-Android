package se.ju.student.android_mjecipes.APIHandler;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import se.ju.student.android_mjecipes.Entities.Account;
import se.ju.student.android_mjecipes.Entities.Comment;
import se.ju.student.android_mjecipes.Entities.JWToken;
import se.ju.student.android_mjecipes.Entities.Recipe;

public class AccountHandler extends Handler {
    private static AccountHandler instance;
    private static final String TAG = "AccountHandler";

    private AccountHandler() {
        super();
    }

    public Account getAccount(String id) {
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

    public String postAccount(Account account) {
        if (account == null) return "";

        String passwordstr = "password/";
        HttpURLConnection connection = null;
        Scanner s = null;
        PrintWriter pw = null;
        String toReturn = "";

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
                    s = new Scanner(connection.getInputStream());
                    toReturn = getLocation(s.nextLine());
                    errors.HTTPCode = Errors.HTTP_CREATED;
                    break;
                default:
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

    // FIXME: 09/11/2016 when specification fixed
    public boolean patchAccount(int id, Account a, JWToken token) {
        if(a == null || token == null) return false;

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

    //public void deleteAccount(int id) { }

    public Recipe[] getRecipes(String id) {
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

    public Comment[] getComments(String id) {
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

    //public void putFavorites(String id, int[] recipeids) { }

    public Recipe[] getFavorites(String id, JWToken token) {
        if(token == null) return null;

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

                    if(recipes != null || recipes.length != 0)
                        for (int i = 0; i < recipes.length; ++i)
                            recipes[i] = Handler.getRecipeHandler().getRecipe(recipes[i].id);
                    break;
                default:
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
