package se.ju.student.android_mjecipes.MjepicesAPIHandler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
        BufferedReader br = null;
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
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                    errors = gson.fromJson(br.readLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    Log.i(TAG, "postRecipe: HTTP Bad Request");
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_CREATED;
                    Log.i(TAG, "postRecipe: HTTP Created");
                    errors.error = getCreatedId(connection.getHeaderField("Location"));
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
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "postRecipe: IO Exception", e);
            }

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
        BufferedReader br = null;
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
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    recipes = gson.fromJson(br.readLine(), Recipe[].class);

                    for(int i = 0; i < recipes.length; ++i)
                        recipes[i] = getRecipe(recipes[i].id);

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
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "getRecipeByPage: IO Exception", e);
            }

            if(connection != null)
                connection.disconnect();
        }

        return recipes;
    }

    @Nullable
    public Recipe getRecipe(int id) {
        BufferedReader br = null;
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
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    recipe = gson.fromJson(br.readLine(), Recipe.class);
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
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "getRecipe: IO Exception", e);
            }
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
        BufferedReader br = null;
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
                    Log.i(TAG, "patchRecipe: HTTP Bad Request");
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                    errors = gson.fromJson(br.readLine(), Errors.class);
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
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "patchRecipe: IO Exception", e);
            }

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

    public boolean postImage(int id, @NonNull InputStream filestream, @NonNull JWToken token) {
        String imagestr = "/image";
        String boundary = "******";
        String hypens = "--";
        String endl = "\r\n";
        int buffersize = 1024 << 10;
        DataOutputStream dos = null;
        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
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
            bis = new BufferedInputStream(filestream);

            buffersize = Math.min(buffersize, bis.available());
            while(bis.read(buffer, 0, buffersize) > 0) {
                dos.write(buffer, 0, buffersize);
                buffersize = Math.min(buffersize, bis.available());
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
                if(bis != null)
                    bis.close();
                if(dos != null)
                    dos.close();
                filestream.close();
            } catch(IOException e) {
                Log.e(TAG, "postImage: IO Exception", e);
            }
        }

        return toReturn;
    }

    public boolean postComment(int id, @NonNull Comment c, @NonNull JWToken token) {
        String commentsstr = "/comments";
        BufferedReader br = null;
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
                    Log.i(TAG, "postComment: HTTP Bad Request");
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                    errors = gson.fromJson(br.readLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_CREATED;
                    Log.i(TAG, "postComment: HTTP Created");
                    errors.error = getCreatedId(connection.getHeaderField("Location"));
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
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "postComment: IO Exception", e);
            }

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
        BufferedReader br = null;
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

    @Nullable
    public Recipe[] search(@NonNull String term) {
        String search = "search?term=";
        BufferedReader br = null;
        HttpURLConnection connection = null;
        Recipe[] recipes = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + RECIPES_URL + search + term).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                    errors = gson.fromJson(br.readLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    Log.i(TAG, "search: HTTP Bad Request");
                    break;
                case HttpURLConnection.HTTP_OK:
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    recipes = gson.fromJson(br.readLine(), Recipe[].class);
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
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "search: IO Exception", e);
            }

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
