package se.ju.student.android_mjecipes.APIHandler;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import se.ju.student.android_mjecipes.Entities.JWToken;
import se.ju.student.android_mjecipes.Entities.Recipe;
import se.ju.student.android_mjecipes.Entities.Comment;

public class RecipeHandler extends Handler {
    private static RecipeHandler instance;
    private static final String TAG = "RecipeHandler";

    private RecipeHandler() {
        super();
    }

    //public void postRecipe(Recipe r) { }

    public Recipe[] getRecipeByPage(int page) {
        String pagestr = "recipes?page=";
        Scanner s = null;
        HttpURLConnection connection = null;
        Recipe[] recipes = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + pagestr + page).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    Log.i(TAG, "getRecipeByPage: HTTP Not Found");
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
            Log.e("RECIPEHANDLER", "doInBackground: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e("RECIPEHANDLER", "doInBackground: IO_ERROR", e);
        } finally {
            if(s != null)
                s.close();
            if(connection != null)
                connection.disconnect();
        }

        return recipes;
    }

    public Recipe getRecipe(int id) {
        Scanner s = null;
        HttpURLConnection connection = null;
        Recipe recipe = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + id).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    Log.i(TAG, "getRecipe: HTTP Not Found");
                    break;
                case HttpURLConnection.HTTP_OK:
                    s = new Scanner(connection.getInputStream());
                    recipe = gson.fromJson(s.nextLine(), Recipe.class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e("RECIPEHANDLER", "doInBackground: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e("RECIPEHANDLER", "doInBackground: IO_ERROR", e);
        } finally {
            if(s != null)
                s.close();
            if(connection != null)
                connection.disconnect();
        }

        return recipe;
    }

    //public void deleteRecipe(int id) { }

    //public void patchRecipe(int id) { }

    //public void postImage(int id, ) { }

    //public void postComment(int id, Comment c) { }

    public Comment[] getComments(int id) {
        String commentstr = "/comments";
        Scanner s = null;
        HttpURLConnection connection = null;
        Comment[] comments = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + id + commentstr).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    Log.i(TAG, "getComments: HTTP Not Found");
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
            Log.e("RECIPEHANDLER", "doInBackground: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e("RECIPEHANDLER", "doInBackground: IO_ERROR", e);
        } finally {
            if(s != null)
                s.close();
            if(connection != null)
                connection.disconnect();
        }

        return comments;
    }

    public Recipe[] search(String term) {
        String search = "search?term=";
        Scanner s = null;
        HttpURLConnection connection = null;
        Recipe[] recipes = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + search + (term != null ? term : "")).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    s = new Scanner(connection.getErrorStream());
                    errors = gson.fromJson(s.nextLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    Log.i(TAG, "search: HTTP Bad Request");
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
            Log.e("RECIPEHANDLER", "doInBackground: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e("RECIPEHANDLER", "doInBackground: IO_ERROR", e);
        } finally {
            if(s != null)
                s.close();
            if(connection != null)
                connection.disconnect();
        }

        return recipes;
    }

    static RecipeHandler getInstance() {
        if(instance == null)
            instance = new RecipeHandler();

        return instance;
    }

}
