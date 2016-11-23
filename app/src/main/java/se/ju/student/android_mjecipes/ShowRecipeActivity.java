package se.ju.student.android_mjecipes;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Direction;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;


public class ShowRecipeActivity extends AppCompatActivity {

    private LinearLayout r;
    private TextView recipeidtv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);
        r = (LinearLayout) findViewById(R.id.show_recipe_main);
        recipeidtv = (TextView) findViewById(R.id.show_recipe_id);

       /* ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null || !ni.isConnected()) {
            Snackbar.make(r, "No connection", Snackbar.LENGTH_LONG).setAction("Refresh", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }).show();
        }*/

        final String rID = getIntent().getStringExtra("recipeId");

        new AsyncTask<Integer,Void,Recipe>() {
            @Override
            protected Recipe doInBackground(Integer... p){
                Recipe r = CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readFromCache(rID, Recipe.class);

                if(r == null) {
                    r = Handler.getRecipeHandler().getRecipe(Integer.parseInt(rID));
                    CacheHandler.getJSONJsonCacheHandler(getBaseContext()).writeToCache(r, Recipe.class);
                }

                return r;
            }

            @Override
            protected void onPostExecute(Recipe recipe) {
                setTitle(recipe.name);
                recipeidtv.setText(Integer.toString(recipe.id));
                ((TextView)r.findViewById(R.id.show_recipe_desc)).setText(recipe.description);
                for(Direction d: recipe.directions) {
                    Button b = new Button(getBaseContext());
                    b.setText(d.order + ": " + d.description);
                    ((LinearLayout) r.findViewById(R.id.show_recipes_ll_directions)).addView(b);
                }

            }
        }.execute(1);

    }

}
