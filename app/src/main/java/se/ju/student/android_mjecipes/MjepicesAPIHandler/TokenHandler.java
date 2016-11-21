package se.ju.student.android_mjecipes.MjepicesAPIHandler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;

public class TokenHandler extends Handler {
    private static TokenHandler instance;
    private static final String TAG = "TokenHandler";

    private TokenHandler() {
        super();
    }

    @Nullable
    public JWToken getToken(@NonNull String username, @NonNull String password) {
        BufferedReader br = null;
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

            switch(connection.getResponseCode()) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    Log.i(TAG, "getToken: HTTP Bad Request");
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                    errors = gson.fromJson(br.readLine(), Errors.class);
                    errors.HTTPCode = Errors.HTTP_BAD_REQUEST;
                    break;
                case HttpURLConnection.HTTP_OK:
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    token = gson.fromJson(br.readLine(), JWToken.class);
                    errors.HTTPCode = Errors.HTTP_OK;
                    break;
                default:
                    Log.i(TAG, "getToken: Internal Server Error");
                    errors.HTTPCode = Errors.HTTP_INTERNAL_SERVER_ERROR;
                    break;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "getToken: Malformed URL", e);
        } catch (IOException e) {
            Log.e(TAG, "getToken: IO Exception", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch(IOException e) {
                Log.e(TAG, "getToken: IO Exception", e);
            }

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
