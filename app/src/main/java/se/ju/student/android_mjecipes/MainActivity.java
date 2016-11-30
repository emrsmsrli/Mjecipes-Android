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
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;


public class MainActivity extends AppCompatActivity {

    /*private Button login;
    private Button b;
    private Button listcomment;
    private Button showrecipes;*/
    View vv;
    DrawerLayout drawerLayout;
    private ActionBarDrawerToggle Toggle;
    NavigationView navigationView;



public static String a;
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        MenuItem item =menu.findItem(R.id.search);
        SearchManager searchManager=(SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView=null;

        if(item!=null)
            searchView=(SearchView) item.getActionView();
        if(searchView!=null)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        return super.onCreateOptionsMenu(menu);
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



    @Override
    protected void onResume() {
        super.onResume();

        Intent i=this.getIntent();
        String action= i.getAction();

        if(!(action==Intent.ACTION_DELETE)) {



                if(action==Intent.ACTION_VIEW)
                    showaccountrecipes();

                else if(action==Intent.ACTION_PICK)
                    showfavorite();

                else{
                    whileloginandnotloginjobs();




        }}

        else{
            final LinearLayout l= (LinearLayout) findViewById(R.id.activity_main);
            Snackbar.make(l,"Account deleted", Snackbar.LENGTH_SHORT).show();
            whileloginandnotloginjobs();
                action=null;

            }
    }



    void deleteaccount(){

        drawermenu();

        final LinearLayout l= (LinearLayout) findViewById(R.id.activity_main);

        new AsyncTask<Void,Void,Recipe[]>(){
            @Override
            protected Recipe[] doInBackground(Void... params) {
                JWToken token = Handler.getTokenHandler().getToken(UserAgent.getInstance(getBaseContext()).getUsername(),
                        UserAgent.getInstance(getBaseContext()).getPassword());
                Boolean b=Handler.getAccountHandler().deleteAccount(UserAgent.getInstance(getBaseContext()).getUserID(),token);
                UserAgent.getInstance(getBaseContext()).logout();

                if(b==true) {
                    Snackbar.make(l, "Account deleted", Snackbar.LENGTH_SHORT).show();



                }
                else{
                    Errors e = Handler.getRecipeHandler().getErrors();

                }
                return Handler.getRecipeHandler().getRecipeByPage(1);
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {

                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                for (int i = 0; i < recipes.length; ++i) {
                    inf.inflate(R.layout.main_recipe_layout, (LinearLayout) findViewById(R.id.activity_main));
                    vv = ((LinearLayout) findViewById(R.id.activity_main)).getChildAt(i);
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


    void whileloginandnotloginjobs(){

        drawermenu();

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
                            finish();
                        }
                    });
                }

            }
        }.execute();

    }



    void drawermenu(){
        setContentView(R.layout.activity_main);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        Toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(Toggle);
        Toggle.syncState();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if(UserAgent.getInstance(getBaseContext()).isLoggedIn()){
            navigationView.getMenu().removeItem(R.id.login);
            navigationView.getMenu().removeItem(R.id.signup);

        }
        else if(!(UserAgent.getInstance(getBaseContext()).isLoggedIn())){
            navigationView.getMenu().removeItem(R.id.favoriterecipes);
            navigationView.getMenu().removeItem(R.id.logout);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.myaccount:

                        Intent i1 ;
                        if(UserAgent.getInstance(getBaseContext()).isLoggedIn()){
                            i1=new Intent(MainActivity.this, ShowAccount.class);
                            i1.setAction(Intent.ACTION_USER_PRESENT);}
                        else
                            i1=new Intent(MainActivity.this, LoginActivity.class);

                        startActivity(i1);
                        finish();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.myrecipes:
                        Intent i2;
                        if(UserAgent.getInstance(getBaseContext()).isLoggedIn()){
                            i2 = new Intent(MainActivity.this,MainActivity.class);
                            i2.setAction(Intent.ACTION_VIEW);}
                        else
                            i2 = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i2);
                        finish();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.createarecipe:
                        Intent i3;
                        if(UserAgent.getInstance(getBaseContext()).isLoggedIn()) {
                            i3 = new Intent(getApplicationContext(), CreateRecipe.class);
                        }
                        else
                            i3 = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i3);
                        finish();
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
                        finish();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.login:

                        Intent i7 = new Intent(MainActivity.this, LoginActivity.class);

                        startActivity(i7);finish();

                        drawerLayout.closeDrawers();

                        break;


                    case R.id.logout:
                        UserAgent.getInstance(getBaseContext()).logout();
                        Intent i8 = new Intent(MainActivity.this,MainActivity.class);

                        startActivity(i8);finish();

                        drawerLayout.closeDrawers();

                        break;

                    case R.id.favoriterecipes:
                        Intent i9 = new Intent(MainActivity.this, MainActivity.class);
                        i9.setAction(Intent.ACTION_PICK);
                        startActivity(i9);finish();

                        drawerLayout.closeDrawers();



                }

                return false;
            }
        });


    }


    void showaccountrecipes(){

        drawermenu();

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
                    inf.inflate(R.layout.main_recipe_layout, (LinearLayout) findViewById(R.id.activity_main));
                    final View vv = ((LinearLayout) findViewById(R.id.activity_main)).getChildAt(i);
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


    void showfavorite(){
        drawermenu();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                JWToken token = Handler.getTokenHandler().getToken(UserAgent.getInstance(getBaseContext()).getUsername(),
                        UserAgent.getInstance(getBaseContext()).getPassword());
                return Handler.getAccountHandler().getFavorites(UserAgent.getInstance(getBaseContext()).getUserID(),token);
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {

                if(recipes!=null){

                    LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    for (int i = 0; i < recipes.length; ++i) {
                        inf.inflate(R.layout.main_recipe_layout, (LinearLayout) findViewById(R.id.activity_main));
                        final View vv = ((LinearLayout) findViewById(R.id.activity_main)).getChildAt(i);
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




}
