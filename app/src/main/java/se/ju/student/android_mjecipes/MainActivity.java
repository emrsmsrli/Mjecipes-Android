package se.ju.student.android_mjecipes;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;


public class MainActivity extends AppCompatActivity {
    private static final int CREATE_RECIPE_REQUEST = 0;
    private LinearLayout mainLinearLayout;
    DrawerLayout drawerLayout;
    private ActionBarDrawerToggle Toggle;
    NavigationView navigationView;
    private boolean loaded = false;
    private boolean ordloaded = false;
    private boolean favloaded = false;

    private int page=Integer.MAX_VALUE;
    private int pagef=Integer.MAX_VALUE-1;



public static String a;
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(Toggle.onOptionsItemSelected(item)){
            return true;
        }

        switch(item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }


        return true;
    }



    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);

        Intent i=this.getIntent();
        String action= i.getAction();

        if(action != null) {

            if(action.equals(Intent.ACTION_SEARCH)) {
                setTitle("Search results");
                new AsyncTask<String, Void, Recipe[]>() {
                    @Override
                    protected void onPostExecute(Recipe[] recipes) {
                        if(recipes!= null)
                            inflate(recipes, null);
                    }

                    @Override
                    protected Recipe[] doInBackground(String... params) {
                        return Handler.getRecipeHandler().search(params[0]);
                    }
                }.execute(i.getStringExtra(SearchManager.QUERY));
            } else if(action.equals(Intent.ACTION_DELETE)) {
                final LinearLayout l = (LinearLayout) findViewById(R.id.activity_main);
                Snackbar.make(l, "Account deleted", Snackbar.LENGTH_SHORT).show();
                i.setAction("");
                whileloginandnotloginjobs();
            } else if (action.equals(Intent.ACTION_VIEW))
                showaccountrecipes();
            else if (action.equals(Intent.ACTION_PICK))
                showfavorite();
            else
                whileloginandnotloginjobs();
        } else {
            whileloginandnotloginjobs();
        }
    }



    void whileloginandnotloginjobs(){

        drawermenu();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {

                Recipe r[];
                if(!isConnectionAvailable()) {
                    if(!ordloaded) {

                        r= CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readRecipePage(1);


                        if (r != null) {
                            ordloaded = true;
                            Snackbar.make(mainLinearLayout, getString(R.string.no_connection_cache_first), Snackbar.LENGTH_LONG).show();
                            return r;
                        }
                    }

                    else {
                        Snackbar.make(mainLinearLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                        return null;
                    }
                }

                r = Handler.getRecipeHandler().getRecipeByPage(1);
                if(r != null) {
                    handleCache(r,1);
                    ordloaded = true;
                }

                return r;



            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {
                if(recipes != null)
                    inflate(recipes, null);
            }
        }.execute();

    }



    void drawermenu(){


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        Toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(Toggle);
        Toggle.syncState();
        mainLinearLayout = (LinearLayout) findViewById(R.id.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if(UserAgent.getInstance(getBaseContext()).isLoggedIn()){
            navigationView.getMenu().removeItem(R.id.login);
            navigationView.getMenu().removeItem(R.id.signup);
            ((TextView)navigationView.getHeaderView(0).findViewById(R.id.uname)).setText("");

        }
        else if(!(UserAgent.getInstance(getBaseContext()).isLoggedIn())){
            ((TextView)navigationView.getHeaderView(0).findViewById(R.id.uname)).setText("Not logged in");
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
                            i1=new Intent(MainActivity.this, ShowAccountActivity.class);
                            i1.setAction(Intent.ACTION_USER_PRESENT);}
                        else
                            i1=new Intent(MainActivity.this, LoginActivity.class);
                        ordloaded=false;
                        startActivity(i1);
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.myrecipes:
                        Intent i2;
                        if(UserAgent.getInstance(getBaseContext()).isLoggedIn()){
                            i2 = new Intent(MainActivity.this,MainActivity.class);
                            i2.setAction(Intent.ACTION_VIEW);}
                        else
                            i2 = new Intent(MainActivity.this, LoginActivity.class);
                        ordloaded=false;
                        startActivity(i2);
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.createarecipe:
                        Intent i3;
                        if(UserAgent.getInstance(getBaseContext()).isLoggedIn()) {
                            i3 = new Intent(getApplicationContext(), CreateRecipeActivity.class);
                            ordloaded=false;
                            startActivityForResult(i3, CREATE_RECIPE_REQUEST);
                        }
                        else {
                            i3 = new Intent(getApplicationContext(), LoginActivity.class);
                            ordloaded=false;
                            startActivity(i3);
                        }
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.recipeofday:
                        Intent i4 = new Intent(getApplicationContext(), MainActivity.class);
                        i4.setAction("");
                        finish();
                        ordloaded=false;
                        startActivity(i4);
                        drawerLayout.closeDrawers();
                        break;


                    case R.id.signup:
                        Intent i6 = new Intent(MainActivity.this, SignupActivity.class);
                        startActivity(i6);
                        ordloaded=false;
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.login:

                        Intent i7 = new Intent(MainActivity.this, LoginActivity.class);
                        ordloaded=false;
                        startActivity(i7);

                        drawerLayout.closeDrawers();

                        break;


                    case R.id.logout:
                        UserAgent.getInstance(getBaseContext()).logout();
                        Intent i8 = new Intent(MainActivity.this,MainActivity.class);
                        i8.setAction("");
                        ordloaded=false;
                        startActivity(i8);
                        finish();

                        drawerLayout.closeDrawers();

                        break;

                    case R.id.favoriterecipes:
                        Intent i9 = new Intent(MainActivity.this, MainActivity.class);
                        i9.setAction(Intent.ACTION_PICK);
                        ordloaded=false;
                        startActivity(i9);finish();

                        drawerLayout.closeDrawers();



                }

                return false;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case CREATE_RECIPE_REQUEST:
                if(requestCode == RESULT_OK) {
                    Snackbar.make(navigationView, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
                }
        }
    }

    void showaccountrecipes(){

        drawermenu();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                Recipe r[];
                if(!isConnectionAvailable()) {
                    if(!loaded) {

                            r= CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readRecipePage(page);


                        if (r != null) {
                            loaded = true;
                            Snackbar.make(mainLinearLayout, getString(R.string.no_connection_cache_first), Snackbar.LENGTH_LONG).show();
                            return r;
                        }
                    } else {
                        Snackbar.make(mainLinearLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                        return null;
                    }
                }

                r = Handler.getAccountHandler().getRecipes(UserAgent.getInstance(getBaseContext()).getUserID());
                if(r != null) {
                    handleCache(r,page);
                    loaded = true;
                }

                return r;


            }



            @Override
            protected void onPostExecute(Recipe[] recipes) {

                if(recipes!=null){
                    inflate(recipes, UserAgent.getInstance(getBaseContext()).getUsername());
            }

            }
        }.execute();
    }

    private void inflate(final Recipe[] recipes, String username) {

        LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (int i = 0; i < recipes.length; ++i) {
            inf.inflate(R.layout.recipe_entry, (LinearLayout) findViewById(R.id.activity_main));
            final View vv = ((LinearLayout) findViewById(R.id.activity_main)).getChildAt(i);
            ((TextView) vv.findViewById(R.id.main_recipe_id)).setText(String.format(Locale.ENGLISH, "%d", recipes[i].id));
            ((TextView) vv.findViewById(R.id.main_recipe_name)).setText(recipes[i].name);
            ((TextView) vv.findViewById(R.id.main_recipe_date)).setText(sdf.format(new Date(recipes[i].created * 1000)));
            ((TextView) vv.findViewById(R.id.main_recipe_description)).setText(recipes[i].description);
            ((TextView) vv.findViewById(R.id.main_recipe_creatorname)).setText(username != null ? username : (recipes[i].creator == null ? "" : recipes[i].creator.userName));
            if (recipes[i].image != null) {
                final ImageView iv = (ImageView) vv.findViewById(R.id.main_recipe_image);
                CacheHandler.getImageCacheHandler(getBaseContext()).downloadImage(new ImageRequest(recipes[i].image, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        if(iv.getWidth() < response.getWidth())
                            return;
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


    void showfavorite(){
        drawermenu();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {



                Recipe r[];
                if(!isConnectionAvailable()) {
                    if(!favloaded) {

                        r= CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readRecipePage(pagef);


                        if (r != null) {
                            favloaded = true;
                            Snackbar.make(mainLinearLayout, getString(R.string.no_connection_cache_first), Snackbar.LENGTH_LONG).show();
                            return r;
                        }
                    } else {
                        Snackbar.make(mainLinearLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                        return null;
                    }
                }

                JWToken token = Handler.getTokenHandler().getToken(UserAgent.getInstance(getBaseContext()).getUsername(),UserAgent.getInstance(getBaseContext()).getPassword());
                r= Handler.getAccountHandler().getFavorites(UserAgent.getInstance(getBaseContext()).getUserID(),token);

                if(r != null) {
                    handleCache(r,pagef);
                    favloaded = true;
                }

                return r;





            }



            @Override
            protected void onPostExecute(Recipe[] recipes) {

                if(recipes!=null){

                    inflate(recipes, null);

                }

            }
        }.execute();


    }
    private boolean isConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni !=null && ni.isConnected();
    }
    private void handleCache(Recipe r[],int page) {
        CacheHandler.getJSONJsonCacheHandler(this).clearRecipePage(page);
        CacheHandler.getJSONJsonCacheHandler(this).writeRecipePage(r, page);
    }


}
