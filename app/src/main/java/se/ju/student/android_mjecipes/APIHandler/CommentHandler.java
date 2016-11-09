package se.ju.student.android_mjecipes.APIHandler;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import se.ju.student.android_mjecipes.Entities.Comment;
import se.ju.student.android_mjecipes.Entities.JWToken;

public class CommentHandler extends Handler {
    private static CommentHandler instance;
    private static final String TAG = "CommentHandler";

    private CommentHandler() {
        super();
    }

    public boolean patchComment(int id, @NonNull Comment comment, @NonNull JWToken token) {
        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + COMMENTS_URL + id).openConnection();
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Authentication", "Bearer " + token.access_token);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            pw = new PrintWriter(connection.getOutputStream());
            pw.print(gson.toJson(comment, Comment.class));
            pw.flush();

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "patchComment: HTTP Unauthorized");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "patchComment: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    s = new Scanner(connection.getErrorStream());
                    errors = gson.fromJson(s.nextLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_NO_CONTENT;
                    break;
                default:
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "patchComment: MALFORMED_URL", e);
        } catch(IOException e) {
            Log.e(TAG, "patchComment: IO_ERROR", e);
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

    //public void deleteComment(int id) { }

    //public void postImage(int id, Image i); { }

    static CommentHandler getInstance() {
        if(instance == null)
            instance = new CommentHandler();

        return instance;
    }

}
