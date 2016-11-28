package se.ju.student.android_mjecipes.MjepicesAPIHandler;

import com.google.gson.Gson;

public abstract class Handler {

    protected static final String API_URL = "http://52.211.99.140/api/v1/";
    protected static final String ACCOUNTS_URL = "accounts/";
    protected static final String TOKENS_URL = "tokens/password";
    protected static final String RECIPES_URL = "recipes/";
    protected static final String COMMENTS_URL = "comments/";
    protected static final Gson gson = new Gson();

    protected Errors errors;

    Handler() {
        errors = new Errors();
    }

    public Errors getErrors() {
        return errors;
    }

    protected String getCreatedId(String location) {
        String[] tokens = location.split("/");
        return tokens[tokens.length - 1];
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
