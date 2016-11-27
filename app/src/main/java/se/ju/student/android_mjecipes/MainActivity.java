package se.ju.student.android_mjecipes;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import se.ju.student.android_mjecipes.UserAgent.UserAgent;


public class MainActivity extends AppCompatActivity {

    /*private Button login;
    private Button b;
    private Button listcomment;
    private Button showrecipes;*/

    DrawerLayout drawerLayout;
    private ActionBarDrawerToggle Toggle;
    NavigationView navigationView;
    private ListView recipeList;


public static String a;
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(Toggle.onOptionsItemSelected(item)){
            return true;
        }
        int id=item.getItemId();
        onSearchRequested();

        return super.onOptionsItemSelected(item);
    }


    void whilenotloginjobs(){
        setContentView(R.layout.activity_main);

        final int[] l = new int[1];
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        Toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(Toggle);
        Toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.myaccount:
                        Intent i1 = new Intent(MainActivity.this, LoginActivity.class);

                        startActivity(i1);
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.myrecipes:
                        Intent i2 = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i2);
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.createarecipe:
                        Intent i3 = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i3);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.recipeofday:
                        Intent i4 = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i4);
                        drawerLayout.closeDrawers();
                        break;


                    case R.id.signup:
                        Intent i6 = new Intent(MainActivity.this, SignupActivity.class);
                        int sayi = ((int) (Math.random() * 3));
                        i6.putExtra("recipeId", Integer.toString(sayi));
                        startActivity(i6);
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.login:

                        Intent i7 = new Intent(MainActivity.this, LoginActivity.class);

                        startActivity(i7);
                        drawerLayout.closeDrawers();

                        break;


                }

                return false;
            }
        });


        final Intent i = getIntent();
/*

            if (i.getAction().equals(Intent.ACTION_SEARCH)) {
                //search with i.getStringExtra(SearchManager.QUERY);
            }
            */

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                return Handler.getRecipeHandler().getRecipeByPage(1);
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {

                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                l[0] = recipes.length;
                for (int i = 0; i < recipes.length; ++i) {
                    inf.inflate(R.layout.main_recipe_layout, (LinearLayout) findViewById(R.id.activity_main));
                    final View vv = ((LinearLayout) findViewById(R.id.activity_main)).getChildAt(i);
                    ((TextView) vv.findViewById(R.id.main_recipe_id)).setText(Integer.toString(recipes[i].id));
                    ((TextView) vv.findViewById(R.id.main_recipe_name)).setText("Name= " + recipes[i].name);
                    ((TextView) vv.findViewById(R.id.main_recipe_date)).setText(sdf.format(new Date(recipes[i].created * 1000)));
                    ((TextView) vv.findViewById(R.id.main_recipe_description)).setText("Description= " + recipes[i].description);
                    ((TextView) vv.findViewById(R.id.main_recipe_creatorname)).setText("Creator= " + recipes[i].creator.userName);
                    if (recipes[i].image != null) {
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

            }
        }.execute();

    }


    void whileloginjobs(){

        whileloginjobs2();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                return Handler.getRecipeHandler().getRecipeByPage(1);
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {

                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                for (int i = 0; i < recipes.length; ++i) {
                    inf.inflate(R.layout.main_recipe_layout, (LinearLayout) findViewById(R.id.activity_main_login));
                    final View vv = ((LinearLayout) findViewById(R.id.activity_main_login)).getChildAt(i);
                    ((TextView) vv.findViewById(R.id.main_recipe_id)).setText(Integer.toString(recipes[i].id));
                    ((TextView) vv.findViewById(R.id.main_recipe_name)).setText("Name= " + recipes[i].name);
                    ((TextView) vv.findViewById(R.id.main_recipe_date)).setText(sdf.format(new Date(recipes[i].created * 1000)));
                    ((TextView) vv.findViewById(R.id.main_recipe_description)).setText("Description= " + recipes[i].description);
                    ((TextView) vv.findViewById(R.id.main_recipe_creatorname)).setText("Creator= " + recipes[i].creator.userName);
                    if (recipes[i].image != null) {
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

            }
        }.execute();


    }


    void whileloginjobs2(){
        setContentView(R.layout.activity_main_login);
        final Intent i=getIntent();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_login);
        Toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(Toggle);
        Toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view_login);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.myaccount_login:
                        Intent i1 = new Intent(MainActivity.this, ShowAccount.class);
                        i1.setAction(Intent.ACTION_USER_PRESENT);
                        startActivity(i1);
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.myrecipes_login:
                        Intent i2 = new Intent(MainActivity.this, MainActivity.class);
                        i2.setAction(Intent.ACTION_VIEW);
                        startActivity(i2);
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.createarecipe_login:
                        Intent i3 = new Intent(getApplicationContext(), CreateRecipe.class);
                        startActivity(i3);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.recipeofday_login:
                        Intent i4 = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i4);
                        drawerLayout.closeDrawers();
                        break;


                    case R.id.logout_login:
                        UserAgent.getInstance(getBaseContext()).logout();
                        Intent i6 = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(i6);
                        drawerLayout.closeDrawers();
                        break;



                }

                return false;
            }
        });


    }


    void showaccountrecipes(){

        whileloginjobs2();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                return Handler.getAccountHandler().getRecipes(UserAgent.getInstance(getBaseContext()).getUserID());
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {

                if(recipes!=null){

                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                for (int i = 0; i < recipes.length; ++i) {
                    inf.inflate(R.layout.main_recipe_layout, (LinearLayout) findViewById(R.id.activity_main_login));
                    final View vv = ((LinearLayout) findViewById(R.id.activity_main_login)).getChildAt(i);
                    ((TextView) vv.findViewById(R.id.main_recipe_id)).setText(Integer.toString(recipes[i].id));
                    ((TextView) vv.findViewById(R.id.main_recipe_name)).setText("Name= " + recipes[i].name);
                    ((TextView) vv.findViewById(R.id.main_recipe_date)).setText(sdf.format(new Date(recipes[i].created * 1000)));
                    ((TextView) vv.findViewById(R.id.main_recipe_description)).setText("Description= " + recipes[i].description);
                    ((TextView) vv.findViewById(R.id.main_recipe_creatorname)).setText("Creator= " + UserAgent.getInstance(getBaseContext()).getUsername());
                    if (recipes[i].image != null) {
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

            }

            }
        }.execute();
    }


    @Override
    protected void onResume() {
        super.onResume();

        Intent i=this.getIntent();
        String action= i.getAction();
        if(!(action==Intent.ACTION_VIEW)) {
            if (!UserAgent.getInstance(this).isLoggedIn())
                whilenotloginjobs();


            else
                whileloginjobs();
        }

        else {
            if (!UserAgent.getInstance(this).isLoggedIn())
                whilenotloginjobs();


            else
                showaccountrecipes();
        }
    }




}
