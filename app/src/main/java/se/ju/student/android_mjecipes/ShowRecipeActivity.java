package se.ju.student.android_mjecipes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    Button gocomments;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int res_id=item.getItemId();
        switch (res_id){
            case R.id.commentcloud:
                Intent i=new Intent(getApplicationContext(),ShowCommentActivity.class);
                TextView t= (TextView) findViewById(R.id.show_recipe_id);
                i.putExtra("resid",t.getText());
                startActivity(i);
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);
        r = (LinearLayout) findViewById(R.id.show_recipe_main);
        recipeidtv = (TextView) findViewById(R.id.show_recipe_id);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.mipmap.ic_forum_white_24dp);

        SharedPreferences sharedPreferences=getSharedPreferences("mydata",0);
        final String rID =sharedPreferences.getString("recid","Nothing Found");

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

     //   final String rID = getIntent().getStringExtra("recipeId");

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
