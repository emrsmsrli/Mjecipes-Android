package se.ju.student.android_mjecipes;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Arrays;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;

public class RecipePagerAdapter extends FragmentStatePagerAdapter {

    private int pages;
    private Recipe[] recipes;
    private String username;

    public RecipePagerAdapter(FragmentManager fm, Recipe[] recipes, String username) {
        super(fm);
        this.recipes = recipes;
        this.username = username;

        double count = 0;
        for(Recipe r: recipes) {
            if(r == null)
                break;
            count++;
        }
        this.pages = (int)Math.ceil(count / 10);
    }

    @Override
    public Fragment getItem(int position) {
        return RecipePageFragment.newInstance(Arrays.copyOfRange(recipes, position * 10, position * 10 + 10), username);
    }

    @Override
    public int getCount() {
        return pages;
    }

}
