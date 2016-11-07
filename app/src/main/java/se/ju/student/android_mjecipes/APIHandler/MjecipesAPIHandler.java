package se.ju.student.android_mjecipes.APIHandler;

import com.google.gson.Gson;

import java.util.Scanner;

/**
 * Created by Emre on 07/11/2016.
 */

public class MjecipesAPIHandler {

    private static MjecipesAPIHandler instance = null;
    private Scanner s;

    private Gson gson = null;

    private MjecipesAPIHandler() {
        gson = new Gson();
    }

    public static MjecipesAPIHandler getInstance() {
        if(instance == null) {
            instance = new MjecipesAPIHandler();
        }

        return instance;
    }

}
