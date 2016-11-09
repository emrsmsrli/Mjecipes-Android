package se.ju.student.android_mjecipes.APIHandler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

    // FIXME: 09/11/2016 when specification fixed
    @Nullable
    public String postRecipe(@NonNull Recipe r, @NonNull JWToken token) {
        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        String toReturn = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            pw = new PrintWriter(connection.getOutputStream());
            pw.print(gson.toJson(r, Recipe.class));
            pw.flush();

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    Log.i(TAG, "postRecipe: HTTP Unauthroized");
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    s = new Scanner(connection.getErrorStream());
                    errors = gson.fromJson(s.nextLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    Log.i(TAG, "postRecipe: HTTP Bad Request");
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    s = new Scanner(connection.getInputStream());
                    toReturn = getLocation(s.nextLine());
                    errors.HTTPCode = Errors.HTTP_CREATED;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "postRecipe: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "postRecipe: IO_EXCEPTION", e);
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

    @Nullable
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

    // FIXME: 09/11/2016 when specification fixed
    public boolean deleteRecipe(int id, @NonNull JWToken token) {
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + id).openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "deleteRecipe: HTTP Unauthorized");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "deleteRecipe: HTTP Not Found");
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
            Log.e(TAG, "deleteRecipe: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "deleteRecipe: IO_EXCEPTION", e);
        } finally {
            if(connection != null)
                connection.disconnect();
        }

        return toReturn;
    }

    // FIXME: 09/11/2016 when specification fixed
    public boolean patchRecipe(@NonNull Recipe recipe, @NonNull JWToken token) {
        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + recipe.id).openConnection();
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Authorizaton", "Bearer "+ token.access_token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            pw = new PrintWriter(connection.getOutputStream());
            pw.print(gson.toJson(recipe, Recipe.class));
            pw.flush();

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    Log.i(TAG, "patchRecipe: HTTP Unauthorized");
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    Log.i(TAG, "patchRecipe: HTTP Not Found");
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    s = new Scanner(connection.getErrorStream());
                    errors = gson.fromJson(s.nextLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                    errors.HTTPCode = Errors.HTTP_NO_CONTENT;
                    toReturn = true;
                    break;
                default:
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "patchRecipe: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "patchRecipe: IO_EXCEPTION", e);
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
    public boolean postImage(int id, @NonNull String filename, @NonNull JWToken token) {
        String imagesstr = "/images";
        String boundary = "******";
        String hypens = "--";
        String endl = "\r\n";
        int buffersize = 1024*1024;
        DataOutputStream dos = null;
        HttpURLConnection connection = null;
        FileInputStream fis = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + id + imagesstr).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(hypens + boundary + endl);
            dos.writeBytes("Content-Disposition: form-data;name=\"image\";filename=\"" + filename + "\"" + endl + endl);

            byte[] buffer = new byte[buffersize];
            fis = new FileInputStream(new File(filename));

            buffersize = Math.min(buffersize, fis.available());
            while(fis.read(buffer, 0, buffersize) > 0) {
                dos.write(buffer, 0, buffersize);
                buffersize = Math.min(buffersize, fis.available());
            }

            dos.writeBytes(endl + hypens + boundary + hypens + endl);
            dos.flush();

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "postImage: HTTP Unauthorized");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "postImage: HTTP Not Found");
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
            Log.e(TAG, "postImage: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "postImage: IO_EXCEPTION", e);
        } finally {
            if(connection != null)
                connection.disconnect();
            try {
                if(fis != null)
                    fis.close();
                if(dos != null)
                    dos.close();
            } catch(IOException e) {
                Log.e(TAG, "postImage: IO_EXCEPTION", e);
            }
        }

        return toReturn;
    }

    // FIXME: 09/11/2016 when specification fixed
    @Nullable
    public String postComment(int id, @NonNull Comment c, @NonNull JWToken token) {
        String commentsstr = "/comments";
        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        String toReturn = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + id + commentsstr).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            pw = new PrintWriter(connection.getOutputStream());
            pw.print(gson.toJson(c, Comment.class));
            pw.flush();

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "postComment: HTTP Unauthorized");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "postComment: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    s = new Scanner(connection.getErrorStream());
                    errors = gson.fromJson(s.nextLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    s = new Scanner(connection.getInputStream());
                    toReturn = getLocation(s.nextLine());
                    break;
                default:
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "postComment: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "postComment: IO_EXCEPTION", e);
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

    @Nullable
    public Recipe[] search(@NonNull String term) {
        String search = "search?term=";
        Scanner s = null;
        HttpURLConnection connection = null;
        Recipe[] recipes = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + search + term).openConnection();
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
