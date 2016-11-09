package se.ju.student.android_mjecipes.APIHandler;

import com.google.gson.Gson;

public abstract class Handler {

    static String API_URL = "http://52.211.99.140/api/v1/";
    static final String ACCOUNTS_URL = "accounts/";
    static final String TOKENS_URL = "tokens/password";
    static final String RECIPES_URL = "recipes/";
    static final String COMMENTS_URL = "comments/";
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
