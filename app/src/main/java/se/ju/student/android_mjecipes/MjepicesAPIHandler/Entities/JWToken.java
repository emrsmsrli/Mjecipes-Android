package se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities;

import android.util.Base64;

import com.google.gson.Gson;

public class JWToken {

    private class JWTBody {
        public String userId;
    }

    private static final Gson gson = new Gson();

    public static final int HEADER_INDEX = 0;
    public static final int BODY_INDEX = 1;
    public static final int SIGNATURE_INDEX = 2;

    public String access_token;
    public long expires_in;

    public String[] getPieces() {
        return access_token.split("\\.");
    }

    public String getUserID() {
        return gson.fromJson(new String(Base64.decode(getPieces()[BODY_INDEX], Base64.DEFAULT)), JWTBody.class).userId;
    }
}
