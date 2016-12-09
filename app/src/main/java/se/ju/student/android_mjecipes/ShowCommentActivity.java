package se.ju.student.android_mjecipes;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class ShowCommentActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, CreateCommentFragment.OnCommentPostedListener {

    private static final int IMAGE_REQUEST_CODE = 1;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout activityLayout;
    private FloatingActionButton floatingActionButton;
    private boolean loaded = false;
    private boolean imgloaded = false;
    private Uri outputFileUri;
    private String recipeIDExtra;
    private ActionMode actionMode;
    private int commentIdforImage = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.show_comment_swipe);
        activityLayout = (LinearLayout) findViewById(R.id.activity_show_comment);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.create_comment_fab);

        swipeRefreshLayout.setOnRefreshListener(this);
        activityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionMode != null)
                    actionMode.finish();
            }
        });

        if(UserAgent.getInstance(this).isLoggedIn()) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionMode != null)
                        actionMode.finish();
                    FragmentManager fm = getSupportFragmentManager();
                    if (fm.findFragmentByTag("CreateComment") == null) {
                        fm.beginTransaction()
                                .add(R.id.create_comment_fragment_holder, CreateCommentFragment.newInstance("", 0, Integer.parseInt(recipeIDExtra)),
                                        "CreateComment")
                                .addToBackStack("CreateComment")
                                .commit();
                        floatingActionButton.setImageResource(R.drawable.ic_close_white_24dp);
                    } else {
                        fm.popBackStack();
                        floatingActionButton.setImageResource(R.drawable.ic_format_quote_white_24dp);
                    }
                }
            });
        } else
            floatingActionButton.setVisibility(View.GONE);

        recipeIDExtra = getIntent().getStringExtra("resid");

        onRefresh();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        floatingActionButton.setImageResource(R.drawable.ic_format_quote_white_24dp);
        if(floatingActionButton.getVisibility() == View.INVISIBLE)
            floatingActionButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        if(!swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(true);

        new AsyncTask<String, Void, Comment[]>() {
            @Override
            protected Comment[] doInBackground(String... p) {
                Comment[] comments;
                if(!isConnectionAvailable()) {
                    if (!loaded) {
                        comments = CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readCommentsOfRecipe(p[0]);

                        if (comments != null) {
                            loaded = true;
                            Snackbar.make(activityLayout, getString(R.string.no_connection_cache_first), Snackbar.LENGTH_LONG).show();
                            return comments;
                        } else return null;
                    } else {
                        Snackbar.make(activityLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                        return null;
                    }
                }

                comments = Handler.getRecipeHandler().getComments(Integer.parseInt(p[0]));
                if(comments != null) {
                    for(Comment c: comments)
                        CacheHandler.getJSONJsonCacheHandler(getBaseContext()).clearSingleJSONCache(Integer.toString(c.id), Comment.class);
                    CacheHandler.getJSONJsonCacheHandler(getBaseContext()).writeToCache(comments, Comment.class);
                    loaded = true;
                }

                return comments;
            }

            @Override
            protected void onPostExecute(final Comment[] comments) {
                if(comments == null) {
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                } else if(comments.length == 0) {
                    View v = findViewById(R.id.loading_screen);
                    if(v != null && v.getVisibility() == View.VISIBLE)
                        v.setVisibility(View.GONE);

                    v = findViewById(R.id.empty_screen);
                    if(v != null) {
                        v.setVisibility(View.VISIBLE);
                        ((TextView) v.findViewById(R.id.empty_view_text)).setText(getString(R.string.show_comment_no_comment));
                    }

                    return;
                }

                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                activityLayout.removeAllViews();
                for(int i = 0; i < comments.length; ++i) {
                    inf.inflate(R.layout.comment_entry, activityLayout);
                    View v = activityLayout.getChildAt(i);
                    String url = comments[i].image;
                    final String commenterId = comments[i].commenter.id;

                    if(url != null)
                        loadImage(url, v);

                    ((TextView)v.findViewById(R.id.main_comment_id)).setText(String.format(Locale.ENGLISH, "%d", comments[i].id));
                    ((RatingBar)v.findViewById(R.id.main_comment_grade)).setRating(comments[i].grade);

                    ((TextView)v.findViewById(R.id.main_comment_text)).setText(comments[i].text);
                    ((TextView)v.findViewById(R.id.main_comment_commenter)).setText(comments[i].commenter.userName);

                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(actionMode != null)
                                actionMode.finish();
                        }
                    });

                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(final View v) {
                            startActionMode(newActionMode(commenterId, v));
                            return true;
                        }
                    });
                }

                View v = findViewById(R.id.loading_screen);
                if(v != null && v.getVisibility() == View.VISIBLE)
                    v.setVisibility(View.GONE);

                swipeRefreshLayout.setRefreshing(false);
            }

            private ActionMode.Callback newActionMode(final String commenterId, final View v) {
                return new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        if(canCreateActionMode(commenterId)) {
                            actionMode = mode;
                            mode.getMenuInflater().inflate(R.menu.show_comment_action_menu, menu);
                            v.setBackgroundResource(R.color.comment_action_mode_enabled_background);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        actionMode.finish();
                        switch(item.getItemId()) {
                            case R.id.delete:
                                final String commentid = ((TextView)v.findViewById(R.id.main_comment_id)).getText().toString();
                                deleteComment(commentid, v);
                                break;
                            case R.id.edit:
                                FragmentManager fm = getSupportFragmentManager();
                                fm.beginTransaction()
                                        .add(R.id.edit_comment_fragment_holder,
                                                CreateCommentFragment.newInstance(((TextView) v.findViewById(R.id.main_comment_text)).getText().toString(),
                                                        (int)((RatingBar) v.findViewById(R.id.main_comment_grade)).getRating(),
                                                        Integer.parseInt(((TextView) v.findViewById(R.id.main_comment_id)).getText().toString())), "EditComment")
                                        .addToBackStack("EditComment")
                                        .commit();
                                floatingActionButton.setVisibility(View.INVISIBLE);
                                break;
                            case R.id.upload_image:
                                commentIdforImage = Integer.parseInt(((TextView) v.findViewById(R.id.main_comment_id)).getText().toString());
                                openImageIntent();
                                break;
                            default:
                                return false;
                        }
                        return true;
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
                    public void onDestroyActionMode(ActionMode mode) {
                        actionMode = null;
                        v.setBackgroundResource(R.drawable.comment_entry_background);
                    }
                };
            }

            private void loadImage(String url, final View v) {
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
                        final ImageView iv = (ImageView) v.findViewById(R.id.main_comment_image);

                        if (bitmap != null) {
                            iv.setImageBitmap(bitmap);
                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imgloaded = true;
                            return;
                        }

                        if (isConnectionAvailable()) {
                            CacheHandler.getImageCacheHandler(getBaseContext()).downloadImage(new ImageRequest(url, new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    iv.setImageBitmap(response);
                                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    CacheHandler.getImageCacheHandler(getBaseContext()).clearSingleImageCache(url);
                                    CacheHandler.getImageCacheHandler(getBaseContext()).writeToCache(url, response);
                                    imgloaded = true;
                                }
                            }, iv.getWidth(), iv.getHeight(), null, null, null));
                        }
                    }
                }.execute(url);
            }

            private void deleteComment(String commentId, final View v) {
                new AsyncTask<String, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(String... params) {
                        JWToken token = Handler.getTokenHandler().getToken(
                                UserAgent.getInstance(getBaseContext()).getUsername(),
                                UserAgent.getInstance(getBaseContext()).getPassword()
                        );

                        if(token == null) return false;

                        CacheHandler.getJSONJsonCacheHandler(getBaseContext()).clearSingleJSONCache(params[0], Comment.class);
                        return Handler.getCommentHandler().deleteComment(Integer.parseInt(params[0]), token);
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        if(result)
                            activityLayout.removeView(v);
                    }
                }.execute(commentId);
            }

        }.execute(recipeIDExtra);
    }

    @Override
    public void onCommentPosted(boolean posted, Errors errors) {
        if(posted) {
            Snackbar.make(activityLayout, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
            onRefresh();
        } else {
            if(errors.hasError(Errors.COMMENT_COMMENTER_ALREDY_COMMENT))
                Snackbar.make(activityLayout, getString(R.string.error_commenter_alredy_comment), Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_TEXT_MISSING))
                Snackbar.make(activityLayout, getString(R.string.error_comment_text_missing), Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_TEXT_WRONG_LENGTH))
                Snackbar.make(activityLayout, getString(R.string.error_comment_text_wrong_length), Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError(Errors.COMMENT_GRADE_INVALID))
                Snackbar.make(activityLayout, getString(R.string.error_comment_grade_invalid), Snackbar.LENGTH_SHORT).show();
            else if(errors.hasError("NoConnection"))
                Snackbar.make(activityLayout, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
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
                        Snackbar.make(activityLayout, getString(R.string.error_image_upload), Snackbar.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private boolean canCreateActionMode(String commenterId) {
        return isConnectionAvailable() &&
                UserAgent.getInstance(this).isLoggedIn() &&
                UserAgent.getInstance(this).getUserID().equals(commenterId);
    }

    private JWToken getToken() {
        return Handler.getTokenHandler().getToken(
            UserAgent.getInstance(this).getUsername(),
            UserAgent.getInstance(this).getPassword()
        );
    }

    private void uploadImage(InputStream stream) {
        new AsyncTask<InputStream, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(InputStream... params) {
                JWToken token = getToken();

                return token != null && Handler.getCommentHandler().postImage(commentIdforImage, params[0], token);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if(result) {
                    Snackbar.make(activityLayout, getString(R.string.done), Snackbar.LENGTH_SHORT).show();
                    onRefresh();
                } else
                    Snackbar.make(activityLayout, getString(R.string.error_image_upload), Snackbar.LENGTH_SHORT).show();

                commentIdforImage = -1;
            }
        }.execute(stream);
    }

    private boolean isConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni !=null && ni.isConnected();
    }

    public void requestReadPermission() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0) {
            if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                Snackbar.make(activityLayout, getString(R.string.error_permission_needed), Snackbar.LENGTH_SHORT);
            }
        }
    }

}