package se.ju.student.android_mjecipes.Entities;

public class JWToken {
    public String access_token;
    public long expires_in;

    public String[] getPieces() {
        return access_token.split(":");
    }
}
