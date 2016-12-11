package se.ju.student.android_mjecipes;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import java.util.Random;
import java.util.Arrays;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class MainActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RecipePageFragment.OnFragmentInteractionListener,
        View.OnClickListener {

    private static final String ACTION_MY_RECIPES = "Mjecipes.MyRecipes";
    private static final String ACTION_MY_FAVORITES = "Mjecipes.MyFavorites";
    private static final int CREATE_RECIPE_REQUEST = 0;
    private static final int MY_RECIPES_PAGE_CODE = Integer.MAX_VALUE;
    private static final int MY_FAVORITES_PAGE_CODE = Integer.MAX_VALUE - 1;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private ViewPager viewPager;
    private TabLayout tabDots;
    private View emptyScreen;
    private boolean loaded = false;
    private boolean ordloaded = false;
    private boolean favloaded = false;
    int counter=0,k=0;
    boolean b=true;
    int []rec;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String act = getIntent().getAction();
        if(act != null && act.equals(Intent.ACTION_SEARCH))
            menu.findItem(R.id.search).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                break;
            case R.id.refresh:
                //TODO
                break;
            case android.R.id.home:
                drawerToggle.onOptionsItemSelected(item);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void invalidateDrawerMenu() {
        if(UserAgent.getInstance(this).isLoggedIn()){
            ((TextView)navigationView.getHeaderView(0).findViewById(R.id.uname)).setText(UserAgent.getInstance(this).getUsername());
            navigationView.getMenu().removeItem(R.id.login);
            navigationView.getMenu().removeItem(R.id.signup);
        } else {
            ((TextView)navigationView.getHeaderView(0).findViewById(R.id.uname)).setText(getString(R.string.drawer_not_logged_in));
            navigationView.getMenu().removeItem(R.id.favoriterecipes);
            navigationView.getMenu().removeItem(R.id.logout);
        }
    }

    @Override
    public void loaded(boolean isThereRecipes) {
        View v = findViewById(R.id.loading_screen);
        if(v != null)
            v.setVisibility(View.GONE);
        if(!isThereRecipes) {
            v = findViewById(R.id.empty_screen);
            if(v != null)
                v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String action = intent.getAction();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabDots = (TabLayout) findViewById(R.id.tabDots);
        emptyScreen = findViewById(R.id.empty_screen);
        FloatingActionButton createRecipeFab = (FloatingActionButton) findViewById(R.id.create_recipe_fab);

        if(createRecipeFab != null)
            createRecipeFab.setOnClickListener(this);

        if(emptyScreen != null)
            ((TextView)emptyScreen.findViewById(R.id.empty_view_text)).setText(getString(R.string.main_activity_no_recipe));

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        if(action != null) {
            switch(action) {
                case Intent.ACTION_SEARCH:
                    if(createRecipeFab != null)
                        createRecipeFab.setVisibility(View.GONE);
                    if(actionBar != null)
                        actionBar.setDisplayHomeAsUpEnabled(false);
                    loadSearchResults(intent);
                    break;
                case ACTION_MY_RECIPES:
                    loadMyRecipes();
                    break;
                case ACTION_MY_FAVORITES:
                    loadMyFavorites();
                    break;
                default:
                    loadLastPostedRecipes();
                    break;
            }
            return;
        }

        loadLastPostedRecipes();
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, CreateRecipeActivity.class));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.myaccount:
                if(UserAgent.getInstance(getBaseContext()).isLoggedIn()) {
                    intent = new Intent(MainActivity.this, ShowAccountActivity.class);
                    intent.setAction(Intent.ACTION_USER_PRESENT);
                } else
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                ordloaded = false;
                startActivity(intent);
                drawerLayout.closeDrawers();
                break;

            case R.id.myrecipes:
                if(UserAgent.getInstance(getBaseContext()).isLoggedIn()) {
                    intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.setAction(ACTION_MY_RECIPES);
                } else
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                ordloaded=false;
                startActivity(intent);
                drawerLayout.closeDrawers();
                break;
            case R.id.createarecipe:
                if(UserAgent.getInstance(getBaseContext()).isLoggedIn()) {
                    intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
                    ordloaded = false;
                    startActivityForResult(intent, CREATE_RECIPE_REQUEST);
                }
                else {
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    ordloaded=false;
                    startActivity(intent);
                }
                drawerLayout.closeDrawers();
                break;
            case R.id.recipeofday:
                Random rand=new Random();;
                int randomNum = rand.nextInt((50 - 0) + 1) +0;
                Intent i = new Intent(MainActivity.this, ShowRecipeActivity.class);
                i.putExtra("recipeId", ((Integer.toString( rec[randomNum]))));
                i.setAction("");
                startActivity(i);
                        ordloaded = false;

                        drawerLayout.closeDrawers();
                break;
            case R.id.signup:
                intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
                ordloaded = false;
                drawerLayout.closeDrawers();
                break;
            case R.id.login:
                intent = new Intent(MainActivity.this, LoginActivity.class);
                ordloaded = false;
                startActivity(intent);
                drawerLayout.closeDrawers();
                break;
            case R.id.logout:
                UserAgent.getInstance(getBaseContext()).logout();
                intent = new Intent(MainActivity.this, MainActivity.class);
                ordloaded = false;
                startActivity(intent);
                finish();
                break;
            case R.id.favoriterecipes:
                intent = new Intent(MainActivity.this, MainActivity.class);
                intent.setAction(ACTION_MY_FAVORITES);
                ordloaded = false;
                drawerLayout.closeDrawers();
                startActivity(intent);
                break;
            default:
                return false;
        }
        return true;
    }

    private void loadSearchResults(Intent i) {
        setTitle("Search results");
        new AsyncTask<String, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(String... params) {
                return Handler.getRecipeHandler().search(params[0]);
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {
                if(recipes != null) {
                    RecipePagerAdapter recipePagerAdapter = new RecipePagerAdapter(getSupportFragmentManager(), recipes, null);
                    viewPager.setAdapter(recipePagerAdapter);
                    tabDots.setupWithViewPager(viewPager);
                    emptyScreen.setVisibility(View.GONE);
                }
            }
        }.execute(i.getStringExtra(SearchManager.QUERY));
    }

    private void loadLastPostedRecipes(){
        invalidateDrawerMenu();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                rec= new int[50];

                Recipe r[] = null;

                if(!isConnectionAvailable()) {
                    if(!ordloaded) {
                        for(int i = 0; i < 5; ++i) {
                            Recipe[] re = CacheHandler.getJSONJsonCacheHandler(MainActivity.this).readRecipePage(i + 1);
                            if(r == null)
                                r = re;
                            else
                                r = concat(r, re);
                        }

                        if (r != null) {
                            ordloaded = true;
                            Snackbar.make(drawerLayout, getString(R.string.no_connection_cache_first), Snackbar.LENGTH_LONG).show();
                        }

                        return r;
                    } else {
                        Snackbar.make(drawerLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                        return null;
                    }
                }

                for(int i = 0; i < 5; ++i) {
                    Recipe[] re = Handler.getRecipeHandler().getRecipeByPage(i + 1);
                    if(re != null){
                        handleCache(re, i + 1);
                        for(int j=0;j<10;j++){
                            rec[k]= re[j].id;
                            k++;
                        }
                    }
                    if(r == null)
                        r = re;
                    else
                        r = concat(r, re);
                }

                return r;
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {
                if(recipes != null) {
                    RecipePagerAdapter recipePagerAdapter = new RecipePagerAdapter(getSupportFragmentManager(), recipes, null);
                    viewPager.setAdapter(recipePagerAdapter);
                    tabDots.setupWithViewPager(viewPager);
                    emptyScreen.setVisibility(View.GONE);
                }
            }
        }.execute();

    }

    private void loadMyRecipes(){
        invalidateDrawerMenu();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                Recipe r[];
                if(!isConnectionAvailable()) {
                    if(!loaded) {
                        r = CacheHandler.getJSONJsonCacheHandler(MainActivity.this).readRecipePage(MY_RECIPES_PAGE_CODE);
                        if (r != null) {
                            loaded = true;
                            Snackbar.make(drawerLayout, getString(R.string.no_connection_cache_first), Snackbar.LENGTH_LONG).show();
                        }
                        return r;
                    } else {
                        Snackbar.make(drawerLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                        return null;
                    }
                }

                r = Handler.getAccountHandler().getRecipes(UserAgent.getInstance(MainActivity.this).getUserID());
                if(r != null) {
                    handleCache(r, MY_RECIPES_PAGE_CODE);
                    loaded = true;
                }

                return r;
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {
                if(recipes!=null){
                    RecipePagerAdapter recipePagerAdapter = new RecipePagerAdapter(
                            getSupportFragmentManager(),
                            recipes,
                            UserAgent.getInstance(MainActivity.this).getUsername());
                    viewPager.setAdapter(recipePagerAdapter);
                    tabDots.setupWithViewPager(viewPager);
                    emptyScreen.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private void loadMyFavorites(){
        invalidateDrawerMenu();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {
                Recipe r[] = null;

                if(!isConnectionAvailable()) {
                    if(!favloaded) {

                        r = CacheHandler.getJSONJsonCacheHandler(MainActivity.this).readRecipePage(MY_FAVORITES_PAGE_CODE);

                        if (r != null) {
                            favloaded = true;
                            Snackbar.make(drawerLayout, getString(R.string.no_connection_cache_first), Snackbar.LENGTH_LONG).show();
                        }

                        return r;
                    } else {
                        Snackbar.make(drawerLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                        return null;
                    }
                }

                JWToken token = Handler.getTokenHandler().getToken(
                        UserAgent.getInstance(MainActivity.this).getUsername(),
                        UserAgent.getInstance(MainActivity.this).getPassword());
                if(token != null)
                    r = Handler.getAccountHandler().getFavorites(UserAgent.getInstance(MainActivity.this).getUserID(),token);

                if(r != null) {
                    handleCache(r, MY_FAVORITES_PAGE_CODE);
                    favloaded = true;
                }

                return r;
            }


            @Override
            protected void onPostExecute(Recipe[] recipes) {
                if(recipes != null){
                    RecipePagerAdapter recipePagerAdapter = new RecipePagerAdapter(getSupportFragmentManager(), recipes, null);
                    viewPager.setAdapter(recipePagerAdapter);
                    tabDots.setupWithViewPager(viewPager);
                    emptyScreen.setVisibility(View.GONE);
                }
            }
        }.execute();


    }

    private void handleCache(Recipe r[], int page) {
        CacheHandler.getJSONJsonCacheHandler(this).clearRecipePage(page);
        CacheHandler.getJSONJsonCacheHandler(this).writeRecipePage(r, page);
    }

    private boolean isConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case CREATE_RECIPE_REQUEST:
                if(resultCode == RESULT_OK) {
                    Snackbar.make(navigationView, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
                }
        }
    }

    private static <T> T[] concat(T[] first, T[] second) {
        if(first == null || second == null)
            return null;
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


}
