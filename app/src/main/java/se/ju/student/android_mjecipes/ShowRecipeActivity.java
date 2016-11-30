package se.ju.student.android_mjecipes;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Direction;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class ShowRecipeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, CreateCommentFragment.OnCommentPostedListener {
    private static final int IMAGE_REQUEST_CODE = 1;
    private static String rID;

    private Uri outputFileUri;
    private LinearLayout mainLinearLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView recipeidtv;
    private FloatingActionButton floatingActionButton;
    private ImageView imageView;
    private String creatorID = null;
    private boolean loaded = false;
    private boolean imgloaded = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_recipe_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(!UserAgent.getInstance(this).isLoggedIn()) {
            menu.findItem(R.id.make_favorite).setVisible(false);
            menu.findItem(R.id.edit).setVisible(false);
            menu.findItem(R.id.delete).setVisible(false);
            menu.findItem(R.id.upload_image).setVisible(false);
        } else {
            if(!UserAgent.getInstance(this).getUserID().equals(creatorID)) {
                menu.findItem(R.id.edit).setVisible(false);
                menu.findItem(R.id.delete).setVisible(false);
                menu.findItem(R.id.upload_image).setVisible(false);

                if(UserAgent.getInstance(this).hasFavorite(Integer.parseInt(rID)))
                    menu.findItem(R.id.make_favorite).setIcon(R.drawable.ic_favorite_white_24dp);
            } else {
                menu.findItem(R.id.make_favorite).setVisible(false);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.show_comments:
                Intent i = new Intent(this, ShowCommentActivity.class);
                i.putExtra("resid", rID);
                startActivity(i);
                break;
            case R.id.make_favorite:
                favorite();
                break;
            case R.id.edit:
                //TODO
                break;
            case R.id.upload_image:
                openImageIntent();
                break;
            case R.id.delete:
                deleteRecipe();
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
        imageView = (ImageView) findViewById(R.id.show_recipe_img);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_show_recipe_swipe);
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setOnRefreshListener(this);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.ic_forum_white_24dp);
        }

        floatingActionButton = (FloatingActionButton) findViewById(R.id.create_comment_fab);
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
                    floatingActionButton.setImageResource(R.drawable.ic_format_quote_white_24dp);
                }
            }
        });

        if(getIntent().hasExtra("recipeId"))
            rID = getIntent().getStringExtra("recipeId");

        onRefresh();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        floatingActionButton.setImageResource(R.drawable.ic_format_quote_white_24dp);
    }

    @Override
    public void onRefresh() {
        if(!swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(true);

        new AsyncTask<Void, Void, Recipe>() {
            @Override
            protected Recipe doInBackground(Void... p) {
                Recipe r;
                if(!isConnectionAvailable()) {
                    if(!loaded) {
                        r = CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readFromCache(rID, Recipe.class);

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

                r = Handler.getRecipeHandler().getRecipe(Integer.parseInt(rID));
                if(r != null) {
                    handleCache(r);
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

                creatorID = recipe.creator != null ? recipe.creator.id : recipe.creatorId;
                supportInvalidateOptionsMenu();

                if(recipe.image != null)
                    loadImage(recipe.image);

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

                View v = findViewById(R.id.loading_screen);
                if(v != null && v.getVisibility() == View.VISIBLE)
                    v.setVisibility(View.GONE);

                swipeRefreshLayout.setRefreshing(false);
            }

            private void loadImage(String url) {
                new AsyncTask<String, Void, Bitmap>() {
                    private String url;

                    @Override
                    protected Bitmap doInBackground(String... params) {
                        url = params[0];
                        if(imgloaded) return null;
                        return CacheHandler.getImageCacheHandler(getBaseContext()).readFromCache(url);
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if(bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imgloaded = true;
                            return;
                        }

                        if(isConnectionAvailable()) {
                            CacheHandler.getImageCacheHandler(getBaseContext()).downloadImage(new ImageRequest(url, new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    imageView.setImageBitmap(response);
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    CacheHandler.getImageCacheHandler(getBaseContext()).clearSingleImageCache(url);
                                    CacheHandler.getImageCacheHandler(getBaseContext()).writeToCache(url, response);
                                    imgloaded = true;
                                }
                            }, imageView.getWidth(), imageView.getHeight(), null, null, null));
                        }
                    }
                }.execute(url);
            }

        }.execute();
    }

    private void favorite() {
        if(!isConnectionAvailable()) {
            Snackbar.make(mainLinearLayout, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
            return;
        }

        UserAgent.getInstance(this).postFavorite(Integer.parseInt(rID), new UserAgent.FavoriteListener() {
            @Override
            public void onFavoritePosted(boolean posted) {
                if(posted) {
                    Snackbar.make(mainLinearLayout, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
                    invalidateOptionsMenu();
                } else {
                    Snackbar.make(mainLinearLayout, getString(R.string.error_favorite_recipe), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    //private void edit(){}

    private void uploadImage(InputStream stream) {
        new AsyncTask<InputStream, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(InputStream... params) {
                JWToken token = getToken();

                return token != null && Handler.getRecipeHandler().postImage(Integer.parseInt(rID), params[0], token);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if(result) {
                    Snackbar.make(mainLinearLayout, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
                    onRefresh();
                } else
                    Snackbar.make(mainLinearLayout, getString(R.string.error_image_upload), Snackbar.LENGTH_SHORT).show();
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

    private void deleteRecipe() {
        if(!isConnectionAvailable()) {
            Snackbar.make(mainLinearLayout, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                JWToken token = Handler.getTokenHandler().getToken(
                        UserAgent.getInstance(getBaseContext()).getUsername(),
                        UserAgent.getInstance(getBaseContext()).getPassword()
                );

                return token != null && Handler.getRecipeHandler().deleteRecipe(Integer.parseInt(rID), token);
            }

            @Override
            protected void onPostExecute(Boolean deleted) {
                if(deleted) {
                    startActivity(getParentActivityIntent());
                    finish();
                } else
                    Snackbar.make(mainLinearLayout, getString(R.string.error_delete_recipe), Snackbar.LENGTH_SHORT).show();
            }

        }.execute();
    }

    private JWToken getToken() {
        return Handler.getTokenHandler().getToken(
                UserAgent.getInstance(this).getUsername(),
                UserAgent.getInstance(this).getPassword()
        );
    }

    private boolean isConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni !=null && ni.isConnected();
    }

    private void handleCache(Recipe r) {
        CacheHandler.getJSONJsonCacheHandler(this).clearSingleJSONCache(rID, Recipe.class);
        CacheHandler.getJSONJsonCacheHandler(this).writeToCache(r, Recipe.class);
    }

    public void requestReadPermission() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    final boolean isCamera;
                    if (data == null) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        isCamera = action != null && action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                    try {
                        uploadImage(getContentResolver().openInputStream(isCamera ? outputFileUri : data.getData()));
                    } catch(FileNotFoundException e) {
                        Snackbar.make(mainLinearLayout, getString(R.string.error_image_upload), Snackbar.LENGTH_SHORT).show();
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
                Snackbar.make(mainLinearLayout, getString(R.string.error_upload_image_permission_needed), Snackbar.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onCommentPosted(boolean posted, Errors errors) {
        if(posted)
            Snackbar.make(mainLinearLayout, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
         else {
            if(errors.hasError(Errors.COMMENT_COMMENTER_ALREDY_COMMENT))
                Snackbar.make(mainLinearLayout, getString(R.string.error_commenter_alredy_comment), Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_TEXT_MISSING))
                Snackbar.make(mainLinearLayout, getString(R.string.error_comment_text_missing), Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_TEXT_WRONG_LENGTH))
                Snackbar.make(mainLinearLayout, getString(R.string.error_comment_text_wrong_length), Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_GRADE_INVALID))
                Snackbar.make(mainLinearLayout, getString(R.string.error_comment_grade_invalid), Snackbar.LENGTH_SHORT).show();
        }
    }

}
