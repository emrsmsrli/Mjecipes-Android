package se.ju.student.android_mjecipes.APIHandler;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import se.ju.student.android_mjecipes.Entities.JWToken;

public class TokenHandler extends Handler {
    private static TokenHandler instance;
    private static final String TAG = "TokenHandler";

    private TokenHandler() {
        super();
    }

    public JWToken getToken(String username, String password) {
        if(username == null || password == null) throw new NullPointerException();

        Scanner s = null;
        PrintWriter pw = null;
        HttpURLConnection connection = null;
        JWToken token = null;

        try {
            connection = (HttpURLConnection) new URL(API_URL + TOKENS_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            pw = new PrintWriter(connection.getOutputStream());
            pw.print("grant_type=password&username=" + username + "&password=" + password);
            pw.flush();

            if(connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                s = new Scanner(connection.getErrorStream());
                errors = gson.fromJson(s.nextLine(), Errors.class);
                errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                Log.i(TAG, "getToken: HTTP Bad Request");
            } else {
                s = new Scanner(connection.getInputStream());
                token = gson.fromJson(s.nextLine(), JWToken.class);
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "getToken: MALFORMED_URL", e);
        } catch (IOException e) {
            Log.e(TAG, "getToken: IO_EXCEPTION", e);
        } finally {
            if(s != null)
                s.close();
            if(pw != null)
                pw.close();
            if(connection != null)
                connection.disconnect();
        }

        return token;
    }

    static TokenHandler getInstance() {
        if(instance == null)
            instance = new TokenHandler();

        return instance;
    }

}