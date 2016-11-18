package se.ju.student.android_mjecipes.MjepicesAPIHandler;

import android.support.annotation.NonNull;
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

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;

public class CommentHandler extends Handler {
    private static CommentHandler instance;
    private static final String TAG = "CommentHandler";

    private CommentHandler() {
        super();
    }

    public boolean patchComment(@NonNull Comment comment, @NonNull JWToken token) {
        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + COMMENTS_URL + comment.id).openConnection();
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
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
                    Log.i(TAG, "patchComment: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "patchComment: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "patchComment: IO Exception", e);
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

    public boolean deleteComment(int id, @NonNull JWToken token) {
        HttpURLConnection connection = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + COMMENTS_URL + id).openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    Log.i(TAG, "deleteComment: HTTP Unauthorized");
                    errors.HTTPCode = Errors.HTTP_UNAUTHORIZED;
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    Log.i(TAG, "deleteComment: HTTP Not Found");
                    errors.HTTPCode = Errors.HTTP_NOT_FOUND;
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                    toReturn = true;
                    errors.HTTPCode = Errors.HTTP_NO_CONTENT;
                    break;
                default:
                    Log.i(TAG, "deleteComment: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch(MalformedURLException e) {
            Log.e(TAG, "deleteComment: Malformed URL", e);
        } catch(IOException e) {
            Log.e(TAG, "deleteComment: IO Exception", e);
        } finally {
            if(connection != null) connection.disconnect();
        }

        return toReturn;
    }

    public boolean postImage(int id, @NonNull String filename, @NonNull JWToken token) {
        String imagesstr = "/image";
        String boundary = "******";
        String hypens = "--";
        String endl = "\r\n";
        int buffersize = 1024 << 10;
        DataOutputStream dos = null;
        HttpURLConnection connection = null;
        FileInputStream fis = null;
        boolean toReturn = false;

        try {
            connection = (HttpURLConnection) new URL(API_URL + COMMENTS_URL + id + imagesstr).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Bearer " + token.access_token);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(hypens + boundary + endl);
            dos.writeBytes("Content-Disposition: form-data;name=\"image\";filename=\"comment-" + id + ".jpg\"" + endl + endl);

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

    static CommentHandler getInstance() {
        if(instance == null)
            instance = new CommentHandler();

        return instance;
    }

}
