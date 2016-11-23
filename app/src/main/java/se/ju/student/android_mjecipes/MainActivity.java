package se.ju.student.android_mjecipes;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;


public class MainActivity extends AppCompatActivity {

    /*private Button login;
    private Button b;
    private Button listcomment;
    private Button showrecipes;*/

    private ListView recipeList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        onSearchRequested();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();

        if(i.getAction().equals(Intent.ACTION_SEARCH)) {
            //search with i.getStringExtra(SearchManager.QUERY);
        }

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                return Handler.getRecipeHandler().getRecipeByPage(1);
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {

                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                for(int i = 0; i < recipes.length; ++i) {
                    inf.inflate(R.layout.main_recipe_layout, (LinearLayout)findViewById(R.id.activity_main));
                    final View vv = ((LinearLayout)findViewById(R.id.activity_main)).getChildAt(i);
                    ((TextView) vv.findViewById(R.id.main_recipe_id)).setText(Integer.toString(recipes[i].id));
                    ((TextView) vv.findViewById(R.id.main_recipe_name)).setText("Name= "+recipes[i].name);
                    ((TextView) vv.findViewById(R.id.main_recipe_date)).setText(sdf.format(new Date(recipes[i].created * 1000)));
                   ((TextView)vv.findViewById(R.id.main_recipe_description)).setText("Description= "+recipes[i].description);
                    ( (TextView)vv.findViewById(R.id.main_recipe_creatorname)).setText("Creator= "+recipes[i].creator.userName);
                    if(recipes[i].image != null) {
                        final ImageView iv = (ImageView) vv.findViewById(R.id.main_recipe_image);
                        CacheHandler.getImageCacheHandler(getBaseContext()).downloadImage(new ImageRequest(recipes[i].image, new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                iv.setImageBitmap(response);
                                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }
                        }, iv.getWidth(), iv.getHeight(), null, null, null));
                    }

                    vv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getApplicationContext(), ShowRecipeActivity.class);
                            i.putExtra("recipeId", ((TextView) vv.findViewById(R.id.main_recipe_id)).getText());
                            startActivity(i);
                        }
                    });
                }

                //recipeList.setAdapter(new ArrayAdapter<>(getBaseContext(), R.layout.main_recipe_layout, recipes));
                //recipeList.addView(new Button(getBaseContext()));

                /*for(int x = 0; x < recipes.length; ++x) {
                    final View recipe = recipeList.getChildAt(x);

                    ((TextView)recipe.findViewById(R.id.main_recipe_name)).setText(recipes[x].name);

                    if(recipes[x].image != null) {
                        CacheHandler.getImageCacheHandler(getBaseContext()).addRequest(new ImageRequest(recipes[x].image, new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                ((ImageView) recipe.findViewById(R.id.main_recipe_image)).setImageBitmap(response);
                            }
                        }, 200, 100, null, null, null));
                    }

                    ((TextView)recipe.findViewById(R.id.main_recipe_date)).setText(new Date(recipes[x].created * 1000).toString());
                }*/
            }
        }.execute();






        /***
        final ImageView view = (ImageView) findViewById(R.id.testimgview);
        final String url = "https://s3-eu-west-1.amazonaws.com/mjecipes-andriod-development/recipe-1";

        login = (Button) findViewById(R.id.button);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... p) {
                        Bitmap b = CacheHandler.getImageCacheHandler(getBaseContext()).readFromCache(url);
                        return b;
                    }

                    @Override
                    protected void onPostExecute(Bitmap b) {
                        if (b == null) {
                            CacheHandler.getImageCacheHandler(getBaseContext()).addRequest(new ImageRequest(url,
                                    new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            CacheHandler.getImageCacheHandler(getBaseContext()).writeToCache(url, response);
                                            view.setImageBitmap(response);
                                        }
                                    }, 100, 100, null, null, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }));
                        } else view.setImageBitmap(b);
                    }
                }.execute();
            }
        });

        b = (Button) findViewById(R.id.butforsignin);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... p) {
                        CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readFromCache("1", Recipe.class);
                        return null;
                    }
                }.execute();
            }
        });


        listcomment = (Button) findViewById(R.id.buttoncomment);

        listcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent c= new Intent(MainActivity.this,ShowCommentActivity.class);
                startActivity(c);
            }
        });

        showrecipes = (Button) findViewById(R.id.butrec);

        showrecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent c= new Intent(MainActivity.this,ShowRecipeActivity.class);
                startActivity(c);
            }
        });*/

    }

}
