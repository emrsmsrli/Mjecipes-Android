package se.ju.student.android_mjecipes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.util.Locale;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Direction;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;

public class ShowRecipeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, CreateCommentFragment.OnCommentPostedListener {
    private static String rID;

    private LinearLayout mainLinearLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView recipeidtv;
    private FloatingActionButton floatingActionButton;
    private boolean loaded = false;
    private boolean imgloaded = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.commentcloud:
                Intent i=new Intent(getApplicationContext(),ShowCommentActivity.class);
                TextView t= (TextView) findViewById(R.id.show_recipe_id);
                i.putExtra("resid",t.getText());
                startActivity(i);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);

        mainLinearLayout = (LinearLayout) findViewById(R.id.show_recipe_main);
        recipeidtv = (TextView) findViewById(R.id.show_recipe_id);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_show_recipe_swipe);
        swipeRefreshLayout.setOnRefreshListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.ic_forum_white_24dp);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.main_recipe_create_comment_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                if(fm.findFragmentByTag("CreateComment") == null) {
                    fm.beginTransaction()
                            .add(R.id.create_comment_fragment_holder, CreateCommentFragment.newInstance("", 0, Integer.parseInt(recipeidtv.getText().toString())), "CreateComment")
                            .addToBackStack("CreateComment")
                            .commit();
                    floatingActionButton.setImageResource(R.drawable.ic_close_white_24dp);
                } else {
                    fm.popBackStack();
                    floatingActionButton.setImageResource(R.drawable.ic_edit_white_24dp);
                }
            }
        });

        if(getIntent().hasExtra("recipeId"))
            rID = getIntent().getStringExtra("recipeId");

        onRefresh();
    }

    @Override
    public void onRefresh() {
        if(!swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(true);

        new AsyncTask<Integer, Void, Recipe>() {
            @Override
            protected Recipe doInBackground(Integer... p) {
                Recipe r;

                if(!loaded) {
                    r = CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readFromCache(rID, Recipe.class);

                    if (r != null) {
                        loaded = true;
                        return r;
                    }
                }

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni == null || !ni.isConnected()) {
                    Snackbar.make(mainLinearLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                    return null;
                }

                r = Handler.getRecipeHandler().getRecipe(Integer.parseInt(rID));
                if(r != null) {
                    CacheHandler.getJSONJsonCacheHandler(getBaseContext()).writeToCache(r, Recipe.class);
                    loaded = true;
                }

                return r;
            }

            @Override
            protected void onPostExecute(final Recipe recipe) {
                if(recipe == null) {
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }

                if(recipe.image != null) {
                    new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... params) {
                            if(imgloaded) return null;
                            return CacheHandler.getImageCacheHandler(getBaseContext()).readFromCache(recipe.image);
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            final ImageView iv = (ImageView) mainLinearLayout.findViewById(R.id.show_recipe_img);

                            if(bitmap != null) {
                                iv.setImageBitmap(bitmap);
                                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                imgloaded = true;
                                return;
                            }

                            CacheHandler.getImageCacheHandler(getBaseContext()).downloadImage(new ImageRequest(recipe.image, new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    iv.setImageBitmap(response);
                                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    CacheHandler.getImageCacheHandler(getBaseContext()).writeToCache(recipe.image, response);
                                    imgloaded = true;
                                }
                            }, iv.getWidth(), iv.getHeight(), null, null, null));
                        }
                    }.execute();
                }

                setTitle(recipe.name);
                recipeidtv.setText(String.format(Locale.ENGLISH, "%d", recipe.id));
                ((TextView) mainLinearLayout.findViewById(R.id.show_recipe_desc)).setText(recipe.description);

                ((LinearLayout) mainLinearLayout.findViewById(R.id.show_recipes_ll_directions)).removeAllViews();
                for (Direction d : recipe.directions) {
                    Button b = new Button(getBaseContext());
                    b.setText(d.description);
                    b.setClickable(false);
                    ((LinearLayout) mainLinearLayout.findViewById(R.id.show_recipes_ll_directions)).addView(b);
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        }.execute(1);
    }

    @Override
    public void onCommentPosted(boolean posted) {
        if(posted)
            Snackbar.make(mainLinearLayout, "Comment posted", Snackbar.LENGTH_SHORT).show();
         else {
            Errors errors = Handler.getRecipeHandler().getErrors();
            if(errors.hasError(Errors.COMMENT_COMMENTER_ALREDY_COMMENT))
                Snackbar.make(mainLinearLayout, "Comment not posted, you have already comment", Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_TEXT_MISSING))
                Snackbar.make(mainLinearLayout, "Comment not posted, text is missing", Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_TEXT_WRONG_LENGTH))
                Snackbar.make(mainLinearLayout, "Comment not posted, wrong text length", Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_GRADE_INVALID))
                Snackbar.make(mainLinearLayout, "Comment not posted, missing grade", Snackbar.LENGTH_SHORT).show();
        }
        getSupportFragmentManager().popBackStack();
        floatingActionButton.setImageResource(R.drawable.ic_edit_white_24dp);
    }
}
