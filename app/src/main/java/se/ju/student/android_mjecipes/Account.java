package se.ju.student.android_mjecipes;

import android.text.Editable;

/**
 * Created by Gizem Alpaydın on 6.11.2016.
 */

public class Account {


    //public int aid;
    public String username;
    //public double latitude;
    //public double longitute;
    public String password;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
