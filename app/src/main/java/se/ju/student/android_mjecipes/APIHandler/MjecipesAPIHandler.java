package se.ju.student.android_mjecipes.APIHandler;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import se.ju.student.android_mjecipes.Entities.*;

public class MjecipesAPIHandler {

    private static class AccountHandler {
        private static AccountHandler instance;
        private Account accountRef;
        private Recipe[] recipesRef;
        private Comment[] commentsRef;

        private AccountHandler() {
            instance = null;
            accountRef = null;
            recipesRef = null;
            commentsRef = null;
        }

        public Account getAccount(int id) {
            new AsyncTask<Integer, Void, Account>() {
                protected void onPreExecute() {
                    accountRef = null;
                }

                protected Account doInBackground(Integer... id) {
                    Scanner s = null;
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_URL + accounts + id[0]).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            connection.disconnect();
                            connection = null;
                            return null;
                        }

                        s = new Scanner(connection.getInputStream());
                        String body = s.nextLine();

                        return gson.fromJson(body, Account.class);
                    } catch (MalformedURLException e) {
                        Log.e("ACCOUNTHANDLER", "doInBackground: MALFORMED_URL", e);
                    } catch (java.io.IOException e) {
                        Log.e("ACCOUNTHANDLER", "doInBackground: IO_ERROR", e);
                    } finally {
                        if(s != null)
                            s.close();
                        if(connection != null)
                            connection.disconnect();
                    }

                    return null;
                }

                protected void onPostExecute(Account a) {
                    accountRef = a;
                }
            }.execute(id);

            return accountRef;
        }

        /*public void postAccount(Account a) { String urlPassword = "password/"; }*/

        //public void patchAccount(int id, Account a) { }

        //public void deleteAccount(int id) { }

        public Recipe[] getRecipes(int id) {
            new AsyncTask<Integer, Void, Recipe[]>() {
                protected void onPreExecute() {
                    recipesRef = null;
                }

                protected Recipe[] doInBackground(Integer... id) {
                    String r = "/recipes";
                    Scanner s = null;
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_URL + accounts + id[0] + r).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            connection.disconnect();
                            connection = null;
                            return null;
                        }

                        s = new Scanner(connection.getInputStream());
                        String body = s.nextLine();

                        return gson.fromJson(body, Recipe[].class);
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

                    return null;
                }

                protected void onPostExecute(Recipe[] r) {
                    recipesRef = r;
                }
            }.execute(id);

            return recipesRef;
        }

        public Comment[] getComments(int id) {
            new AsyncTask<Integer, Void, Comment[]>() {
                protected void onPreExecute() {
                    recipesRef = null;
                }

                protected Comment[] doInBackground(Integer... id) {
                    String c = "/comments";
                    Scanner s = null;
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_URL + accounts + id[0] + c).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            connection.disconnect();
                            connection = null;
                            return null;
                        }

                        s = new Scanner(connection.getInputStream());
                        String body = s.nextLine();

                        return gson.fromJson(body, Comment[].class);
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

                    return null;
                }

                protected void onPostExecute(Comment[] c) {
                    commentsRef = c;
                }
            }.execute(id);

            return commentsRef;
        }

        //public void putFavorites(int id, int[] recipeids) { }

        public Recipe[] getFavorites(int id) {
            new AsyncTask<Integer, Void, Recipe[]>() {
                protected void onPreExecute() {
                    recipesRef = null;
                }

                protected Recipe[] doInBackground(Integer... id) {
                    String f = "/favorites";
                    Scanner s = null;
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_URL + accounts + id[0] + f).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            connection.disconnect();
                            connection = null;
                            return null;
                        }

                        s = new Scanner(connection.getInputStream());
                        String body = s.nextLine();

                        class idclass {
                            public int id;
                        }

                        idclass[] ids = gson.fromJson(body, idclass[].class);
                        Recipe[] r = new Recipe[ids.length];

                        for(int i = 0; i < ids.length; ++i)
                            r[i] = MjecipesAPIHandler.getInstance().getRecipeHandler().getRecipe(ids[i].id);

                        return r;
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

                    return null;
                }

                protected void onPostExecute(Recipe[] r) {
                    recipesRef = r;
                }
            }.execute(id);

            return recipesRef;
        }

        public static AccountHandler getInstance() {
            if(instance == null)
                instance = new AccountHandler();

            return instance;
        }
    }

    private static class TokenHandler {
        private static TokenHandler instance;

        private TokenHandler() {

        }

        //public JsonToken/*TODO return type*/ getToken(String username, String password) { }

        public static TokenHandler getInstance() {
            if(instance == null)
                instance = new TokenHandler();

            return instance;
        }
    }

    private static class RecipeHandler {
        private static RecipeHandler instance;
        private Recipe[] recipesRef;
        private Comment[] commentsRef;
        private Recipe recipeRef;

        private RecipeHandler() {

        }

        //public void postRecipe(Recipe r) { }

        public Recipe[] getRecipeByPage(int page) {
            new AsyncTask<Integer, Void, Recipe[]>() {
                protected void onPreExecute() {
                    recipesRef = null;
                }

                protected Recipe[] doInBackground(Integer... page) {
                    String url = "recipes?page=";
                    Scanner s = null;
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_URL + url + page[0]).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            connection.disconnect();
                            connection = null;
                            return null;
                        }

                        s = new Scanner(connection.getInputStream());
                        String body = s.nextLine();

                        return gson.fromJson(body, Recipe[].class);
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

                    return null;
                }

                protected void onPostExecute(Recipe[] r) {
                    recipesRef = r;
                }
            }.execute(page);

            return recipesRef;
        }

        public Recipe getRecipe(int id) {
            new AsyncTask<Integer, Void, Recipe>() {
                protected void onPreExecute() {
                    recipeRef = null;
                }

                protected Recipe doInBackground(Integer... id) {
                    Scanner s = null;
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_URL + recipes + id[0]).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            connection.disconnect();
                            connection = null;
                            return null;
                        }

                        s = new Scanner(connection.getInputStream());
                        String body = s.nextLine();

                        return gson.fromJson(body, Recipe.class);
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

                    return null;
                }

                protected void onPostExecute(Recipe r) {
                    recipeRef = r;
                }
            }.execute(id);

            return recipeRef;
        }

        //public void deleteRecipe(int id) { }

        //public void patchRecipe(int id) { }

        //public void postImage(int id, ) { }

        //public void postComment(int id, Comment c) { }

        public Comment[] getComments(int id) {
            new AsyncTask<Integer, Void, Comment[]>() {
                protected void onPreExecute() {
                    commentsRef = null;
                }

                protected Comment[] doInBackground(Integer... id) {
                    String c = "/comments";
                    Scanner s = null;
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_URL + recipes + id[0] + c).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                            connection.disconnect();
                            connection = null;
                            return null;
                        }

                        s = new Scanner(connection.getInputStream());
                        String body = s.nextLine();

                        return gson.fromJson(body, Comment[].class);
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

                    return null;
                }

                protected void onPostExecute(Comment[] r) {
                    commentsRef = r;
                }
            }.execute(id);

            return commentsRef;
        }

        public Recipe[] search(String term) {
            new AsyncTask<String, Void, Recipe[]>() {
                protected void onPreExecute() {
                    commentsRef = null;
                }

                protected Recipe[] doInBackground(String... term) {
                    String url = "search?term=";
                    Scanner s = null;
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_URL + recipes + url + term[0]).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                            connection.disconnect();
                            connection = null;
                            return null;
                        }

                        s = new Scanner(connection.getInputStream());
                        String body = s.nextLine();

                        return gson.fromJson(body, Recipe[].class);
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

                    return null;
                }

                protected void onPostExecute(Recipe[] r) {
                    recipesRef = r;
                }
            }.execute(term);

            return recipesRef;
        }

        public static RecipeHandler getInstance() {
            if(instance == null)
                instance = new RecipeHandler();

            return instance;
        }
    }

    private static class CommentHandler {
        private static CommentHandler instance;

        private CommentHandler() {

        }

        //public void patchComment(int id, Comment c) { }

        //public void deleteComment(int id) { }

        //public void postImage(int id, Image i); { }

        public static CommentHandler getInstance() {
            if(instance == null)
                instance = new CommentHandler();

            return instance;
        }
    }

    private static final String accounts = "accounts/";
    private static final String tokens = "tokens/";
    private static final String recipes = "recipesRef/";
    private static final String comments = "commentsRef/";

    private static MjecipesAPIHandler instance = null;
    private static Gson gson = null;

    private static String API_URL = "http://52.211.99.140/api/v1/";

    private MjecipesAPIHandler() {
        gson = new Gson();
    }

    public AccountHandler getAccountHandler() {
        return AccountHandler.getInstance();
    }

    public TokenHandler getTokenHandler() {
        return TokenHandler.getInstance();
    }

    public RecipeHandler getRecipeHandler() {
        return RecipeHandler.getInstance();
    }

    public CommentHandler getCommentHandler() {
        return CommentHandler.getInstance();
    }

    public static MjecipesAPIHandler getInstance() {
        if(instance == null) {
            instance = new MjecipesAPIHandler();
        }

        return instance;
    }

    public static MjecipesAPIHandler getInstance(String API_URL_P) {
        API_URL = API_URL_P;
        return getInstance();
    }

}
