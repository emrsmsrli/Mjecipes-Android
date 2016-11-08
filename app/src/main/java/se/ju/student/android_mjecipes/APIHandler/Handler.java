package se.ju.student.android_mjecipes.APIHandler;

import com.google.gson.Gson;

public abstract class Handler {

    static String API_URL = "http://52.211.99.140/api/v1/";
    static final String accounts = "accounts/";
    static final String tokens = "tokens/password";
    static final String recipes = "recipes/";
    static final String comments = "comments/";
    static Gson gson = new Gson();

    Errors errors;

    Handler() {
        errors = null;
    }

    public Errors getErrors() {
        return errors;
    }


    public static AccountHandler getAccountHandler() {
        return AccountHandler.getInstance();
    }

    public static TokenHandler getTokenHandler() {
        return TokenHandler.getInstance();
    }

    public static RecipeHandler getRecipeHandler() {
        return RecipeHandler.getInstance();
    }

    public static CommentHandler getCommentHandler() {
        return CommentHandler.getInstance();
    }
}
