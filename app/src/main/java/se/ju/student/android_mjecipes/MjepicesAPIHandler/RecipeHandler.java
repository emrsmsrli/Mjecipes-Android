package se.ju.student.android_mjecipes.MjepicesAPIHandler;

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

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;

public class RecipeHandler extends Handler {
    private static RecipeHandler instance;
    private static final String TAG = "RecipeHandler";

    private RecipeHandler() {
        super();
    }

    public boolean postRecipe(@NonNull Recipe r, @NonNull JWToken token) {
        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        boolean toReturn = false;

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
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_CREATED;
                    break;
                default:
                    Log.i(TAG, "postRecipe: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "postRecipe: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "postRecipe: IO Exception", e);
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
                    Log.i(TAG, "getRecipeByPage: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "getRecipeByPage: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "getRecipeByPage: IO Exception", e);
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
                    Log.i(TAG, "getRecipe: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "getRecipe: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "getRecipe: IO Exception", e);
        } finally {
            if(s != null)
                s.close();
            if(connection != null)
                connection.disconnect();
        }

        return recipe;
    }

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
                    Log.i(TAG, "deleteRecipe: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "deleteRecipe: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "deleteRecipe: IO Exception", e);
        } finally {
            if(connection != null)
                connection.disconnect();
        }

        return toReturn;
    }

    public boolean patchRecipe(@NonNull Recipe recipe, @NonNull JWToken token) {
        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + recipe.id).openConnection();
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
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
                    Log.i(TAG, "patchRecipe: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "patchRecipe: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "patchRecipe: IO Exception", e);
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

    public boolean postImage(int id, @NonNull String filename, @NonNull JWToken token) {
        String imagestr = "/image";
        String boundary = "******";
        String hypens = "--";
        String endl = "\r\n";
        int buffersize = 1024 << 10;
        DataOutputStream dos = null;
        HttpURLConnection connection = null;
        FileInputStream fis = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + id + imagestr).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(hypens + boundary + endl);
            dos.writeBytes("Content-Disposition: form-data;name=\"image\";filename=\"recipe-" + id + ".jpg\"" + endl + endl);

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
                    Log.i(TAG, "postImage: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "postImage: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "postImage: IO Exception", e);
        } finally {
            if(connection != null)
                connection.disconnect();
            try {
                if(fis != null)
                    fis.close();
                if(dos != null)
                    dos.close();
            } catch(IOException e) {
                Log.e(TAG, "postImage: IO Exception", e);
            }
        }

        return toReturn;
    }

    public boolean postComment(int id, @NonNull Comment c, @NonNull JWToken token) {
        String commentsstr = "/comments";
        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        boolean toReturn = false;

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
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_CREATED;
                    break;
                default:
                    Log.i(TAG, "postComment: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "postComment: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "postComment: IO Exception", e);
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
                    Log.i(TAG, "getComments: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "getComments: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "getComments: IO Exception", e);
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
                    Log.i(TAG, "search: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "search: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "search: IO Exception", e);
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
