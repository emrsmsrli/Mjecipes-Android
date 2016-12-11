package se.ju.student.android_mjecipes;

import android.Manifest;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import java.util.Random;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    private static final int RECIPE_EDIT_REQUEST_CODE = 2;
    private static final String ACTION_MY_RECIPES = "Mjecipes.MyRecipes";
    private static final String ACTION_MY_FAVORITES = "Mjecipes.MyFavorites";
    private static final int CREATE_RECIPE_REQUEST = 0;
    private static final int IMAGE_REQUEST_CODE = 1;
    private static final int MY_RECIPES_PAGE_CODE = Integer.MAX_VALUE;
    private static final int MY_FAVORITES_PAGE_CODE = Integer.MAX_VALUE - 1;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private ViewPager viewPager;
    private TabLayout tabDots;
    private View emptyScreen;
    private int currentRID;
    private ActionMode actionMode = null;
    private Uri outputFileUri;
    private boolean loaded = false;
    private boolean ordloaded = false;
    private boolean favloaded = false;
    int k = 0;
    int []rec;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String act = getIntent().getAction();
        if(act != null && act.equals(Intent.ACTION_SEARCH)) {
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.refresh).setVisible(false);
        }

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
                refresh();
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
    public void loaded() {
        View v = findViewById(R.id.loading_screen);
        if(v != null)
            v.setVisibility(View.GONE);
    }

    @Override
    public void onRecipeLongClick(final View v, final int recipeID, String creatorID) {
        if(actionMode != null)
            actionMode.finish();
        final String cID = creatorID != null ? creatorID : UserAgent.getInstance(this).getUserID();
        currentRID = recipeID;
        actionMode = startActionMode(new android.view.ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.main_activity_action_menu, menu);
                v.setBackgroundResource(R.color.colorPrimary);
                onPrepareActionMode(mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                if(UserAgent.getInstance(MainActivity.this).isLoggedIn()) {
                    if(UserAgent.getInstance(MainActivity.this).getUserID().equals(cID)) {
                        menu.findItem(R.id.edit).setVisible(true);
                        menu.findItem(R.id.upload_image).setVisible(true);
                        menu.findItem(R.id.delete).setVisible(true);
                    }

                    menu.findItem(R.id.make_favorite).setVisible(true);
                    if(UserAgent.getInstance(MainActivity.this).hasFavorite(recipeID))
                        menu.findItem(R.id.make_favorite).setIcon(R.drawable.ic_favorite_white_24dp);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.show_comments:
                        Intent i = new Intent(MainActivity.this, ShowCommentActivity.class);
                        i.putExtra("resid", Integer.toString(currentRID));
                        startActivity(i);
                        break;
                    case R.id.make_favorite:
                        favorite(currentRID);
                        break;
                    case R.id.edit:
                        edit(currentRID);
                        break;
                    case R.id.upload_image:
                        openImageIntent();
                        break;
                    case R.id.delete:
                        deleteRecipe(currentRID);
                        break;
                    default:
                        return false;
                }
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {
                actionMode = null;
                RecipePageFragment.actionMode = null;
                v.setBackgroundResource(R.color.colorAccent);
            }
        });
    }

    private void favorite(int rID) {
        if(!isConnectionAvailable()) {
            Snackbar.make(navigationView, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
            return;
        }

        UserAgent.getInstance(this).postFavorite(rID, new UserAgent.FavoriteListener() {
            @Override
            public void onFavoritePosted(boolean posted) {
                if(posted) {
                    Snackbar.make(navigationView, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
                    refresh();
                } else
                    Snackbar.make(navigationView, getString(R.string.error_favorite_recipe), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRecipe(final int rID) {
        if(!isConnectionAvailable()) {
            Snackbar.make(navigationView, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                JWToken token = Handler.getTokenHandler().getToken(
                        UserAgent.getInstance(getBaseContext()).getUsername(),
                        UserAgent.getInstance(getBaseContext()).getPassword()
                );

                return token != null && Handler.getRecipeHandler().deleteRecipe(rID, token);
            }

            @Override
            protected void onPostExecute(Boolean deleted) {
                if(deleted) {
                    Snackbar.make(navigationView, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
                    refresh();
                } else
                    Snackbar.make(navigationView, getString(R.string.error_delete_recipe), Snackbar.LENGTH_SHORT).show();
            }

        }.execute();
    }

    public ActionMode getActionMode() {
        return actionMode;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        final String action = intent.getAction();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabDots = (TabLayout) findViewById(R.id.tabDots);
        emptyScreen = findViewById(R.id.empty_screen);
        FloatingActionButton createRecipeFab = (FloatingActionButton) findViewById(R.id.create_recipe_fab);
        rec= new int[50];
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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(actionMode != null)
                    actionMode.finish();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

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

    public void requestReadPermission() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private JWToken getToken() {
        return Handler.getTokenHandler().getToken(
                UserAgent.getInstance(this).getUsername(),
                UserAgent.getInstance(this).getPassword()
        );
    }

    private void uploadImage(InputStream stream, final int rID) {
        new AsyncTask<InputStream, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(InputStream... params) {
                JWToken token = getToken();

                return token != null && Handler.getRecipeHandler().postImage(rID, params[0], token);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if(result) {
                    Snackbar.make(navigationView, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
                    refresh();
                } else
                    Snackbar.make(navigationView, getString(R.string.error_image_upload), Snackbar.LENGTH_SHORT).show();
            }
        }.execute(stream);
    }

    private void openImageIntent() {
        requestReadPermission();
        final File root = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "Mjecipes");
        if(root.mkdirs())
            return;
        final String fname = "img-" + UUID.randomUUID().toString();

        File sdImageMainDirectory;
        try {
            sdImageMainDirectory = File.createTempFile(fname, ".jpg", root);
        } catch(IOException e) {
            return;
        }

        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, IMAGE_REQUEST_CODE);
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
                Random rand=new Random();
                int randomNum = rand.nextInt(50);
                Intent i = new Intent(MainActivity.this, ShowRecipeActivity.class);
                i.putExtra("recipeId", Integer.toString(rec[randomNum]));
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

    private void refresh() {
        View v = findViewById(R.id.loading_screen);
        if(v != null)
            v.setVisibility(View.VISIBLE);

        if(loaded)
            loadMyRecipes();
        else if(favloaded)
            loadMyFavorites();
        else if(ordloaded)
            loadLastPostedRecipes();
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
                    if(recipes.length == 0) {
                        loaded();
                        emptyScreen.setVisibility(View.VISIBLE);
                        return;
                    }

                    RecipePagerAdapter recipePagerAdapter = new RecipePagerAdapter(getSupportFragmentManager(), recipes, null);
                    viewPager.setAdapter(recipePagerAdapter);
                    tabDots.setupWithViewPager(viewPager);
                }
            }
        }.execute(i.getStringExtra(SearchManager.QUERY));
    }

    private void loadLastPostedRecipes(){
        invalidateDrawerMenu();

        new AsyncTask<Void, Void, Recipe[]>() {
            @Override
            protected Recipe[] doInBackground(Void... p) {


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

                if(r != null)
                    ordloaded = true;
                return r;
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {
                if(recipes != null) {
                    if(recipes.length == 0) {
                        loaded();
                        emptyScreen.setVisibility(View.VISIBLE);
                        return;
                    }

                    RecipePagerAdapter recipePagerAdapter = new RecipePagerAdapter(getSupportFragmentManager(), recipes, null);
                    viewPager.setAdapter(recipePagerAdapter);
                    tabDots.setupWithViewPager(viewPager);
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
                if(recipes != null) {
                    if(recipes.length == 0) {
                        loaded();
                        emptyScreen.setVisibility(View.VISIBLE);
                        return;
                    }

                    RecipePagerAdapter recipePagerAdapter = new RecipePagerAdapter(
                            getSupportFragmentManager(),
                            recipes,
                            UserAgent.getInstance(MainActivity.this).getUsername());
                    viewPager.setAdapter(recipePagerAdapter);
                    tabDots.setupWithViewPager(viewPager);
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
                    if(recipes.length == 0) {
                        loaded();
                        emptyScreen.setVisibility(View.VISIBLE);
                        return;
                    }

                    RecipePagerAdapter recipePagerAdapter = new RecipePagerAdapter(getSupportFragmentManager(), recipes, null);
                    viewPager.setAdapter(recipePagerAdapter);
                    tabDots.setupWithViewPager(viewPager);
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
                    refresh();
                }
                break;
            case IMAGE_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    final boolean isCamera;
                    if (data == null) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        isCamera = action != null && action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                    try {
                        uploadImage(getContentResolver().openInputStream(isCamera ? outputFileUri : data.getData()), currentRID);
                    } catch(FileNotFoundException e) {
                        Snackbar.make(navigationView, getString(R.string.error_image_upload), Snackbar.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0) {
            if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                Snackbar.make(navigationView, getString(R.string.error_permission_needed), Snackbar.LENGTH_SHORT).show();
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

    private void edit(final int recipeid) {
        new AsyncTask<Void,Void,Recipe>() {
            @Override
            protected Recipe doInBackground(Void... params) {
                return Handler.getRecipeHandler().getRecipe(recipeid);
            }

            @Override
            protected void onPostExecute(Recipe recipe) {
                if(recipe == null) {
                    Snackbar.make(navigationView, getString(R.string.something_went_wrong), Snackbar.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
                intent.putExtra("recipeID", Integer.toString(recipe.id));
                intent.putExtra("recipeName",recipe.name);
                intent.putExtra("recipeDesc", recipe.description);

                String[] directions = new String[recipe.directions.length];
                for(int i = 0; i < directions.length; ++i)
                    directions[i] = recipe.directions[i].description;

                intent.putExtra("recipeDirecs", directions);
                startActivityForResult(intent, RECIPE_EDIT_REQUEST_CODE);
            }
        }.execute();
    }
}
