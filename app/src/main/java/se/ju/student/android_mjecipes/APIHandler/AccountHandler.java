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

    public boolean postAccount(Account a) {
        if (a == null) return false;

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
            String str = gson.toJson(a, Account.class);
            pw.print(str);
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

    //public void patchAccount(int id, Account a) { }

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
