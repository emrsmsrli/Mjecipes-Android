package se.ju.student.android_mjecipes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.util.Locale;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class ShowCommentActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, CreateCommentFragment.OnCommentPostedListener {

    private static final int IMAGE_REQUEST_CODE = 1;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout activityLayout;
    private boolean loaded = false;
    private boolean imgloaded = false;
    private String recipeIDExtra;
    private ActionMode actionMode;
    private int commentIdforImage = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.show_comment_swipe);
        activityLayout = (LinearLayout) findViewById(R.id.activity_show_comment);

        swipeRefreshLayout.setOnRefreshListener(this);
        activityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionMode != null)
                    actionMode.finish();
            }
        });

        recipeIDExtra = getIntent().getStringExtra("resid");

        onRefresh();
    }

    @Override
    public void onRefresh() {
        if(!swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(true);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        new AsyncTask<String, Void, Comment[]>() {

            @Override
            protected Comment[] doInBackground(String... p) {
                Comment[] comments;
                if(!isConnectionAvailable()) {
                    if (!loaded) {
                        comments = CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readCommentsOfRecipe(p[0]);

                        if (comments != null) {
                            loaded = true;
                            return comments;
                        }
                    } else return null;
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
                }

                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                activityLayout.removeAllViews();
                for(int i = 0; i < comments.length; ++i) {
                    inf.inflate(R.layout.comment_entry, activityLayout);
                    final View v = activityLayout.getChildAt(i);
                    final String url = comments[i].image;
                    final String commenterId = comments[i].commenter.id;

                    if(comments[i].image != null) {
                        new AsyncTask<Void, Void, Bitmap>() {
                            @Override
                            protected Bitmap doInBackground(Void... params) {
                                if(imgloaded) return null;
                                return CacheHandler.getImageCacheHandler(getBaseContext()).readFromCache(url);
                            }

                            @Override
                            protected void onPostExecute(Bitmap bitmap) {
                                final ImageView iv = (ImageView) v.findViewById(R.id.main_comment_image);

                                if(bitmap != null) {
                                    iv.setImageBitmap(bitmap);
                                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    imgloaded = true;
                                    return;
                                }

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
                        }.execute();
                    }

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
                            startActionMode(new ActionMode.Callback() {
                                @Override
                                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                                    if(isConnectionAvailable() &&
                                            UserAgent.getInstance(getBaseContext()).isLoggedIn() &&
                                            UserAgent.getInstance(getBaseContext()).getUserID().equals(commenterId)) {
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
                                        case R.id.delete_comment:
                                            final String commentid = ((TextView)v.findViewById(R.id.main_comment_id)).getText().toString();
                                            new AsyncTask<Void, Void, Boolean>() {
                                                @Override
                                                protected Boolean doInBackground(Void... params) {
                                                    JWToken token = Handler.getTokenHandler().getToken(
                                                            UserAgent.getInstance(getBaseContext()).getUsername(),
                                                            UserAgent.getInstance(getBaseContext()).getPassword()
                                                    );

                                                    if(token == null) return null;

                                                    CacheHandler.getJSONJsonCacheHandler(getBaseContext()).clearSingleJSONCache(commentid, Comment.class);
                                                    return Handler.getCommentHandler().deleteComment(Integer.parseInt(commentid), token);
                                                }

                                                @Override
                                                protected void onPostExecute(Boolean result) {
                                                    if(result)
                                                        activityLayout.removeView(v);
                                                }
                                            }.execute();
                                            break;
                                        case R.id.edit_comment:
                                            FragmentManager fm = getSupportFragmentManager();
                                            fm.beginTransaction()
                                                    .add(R.id.create_comment_fragment_holder,
                                                    CreateCommentFragment.newInstance(((TextView) v.findViewById(R.id.main_comment_text)).getText().toString(),
                                                            (int)((RatingBar) v.findViewById(R.id.main_comment_grade)).getRating(),
                                                            Integer.parseInt(((TextView) v.findViewById(R.id.main_comment_id)).getText().toString())), "CreateComment")
                                                    .addToBackStack("CreateComment")
                                                    .commit();
                                            break;
                                        case R.id.upload_image_comment:
                                            commentIdforImage = Integer.parseInt(((TextView) v.findViewById(R.id.main_comment_id)).getText().toString());
                                            Intent i = new Intent();
                                            i.setAction(Intent.ACTION_PICK);
                                            i.setType("image/*");
                                            startActivityForResult(i, IMAGE_REQUEST_CODE);
                                            break;
                                        default:
                                            return false;
                                    }
                                    return true;
                                }

                                @Override
                                public void onDestroyActionMode(ActionMode mode) {
                                    actionMode = null;
                                    v.setBackgroundResource(R.drawable.comment_entry_background);
                                }
                            });
                            return true;
                        }
                    });
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        }.execute(recipeIDExtra);
    }

    @Override
    public void onCommentPosted(boolean posted) {
        if(posted) {
            Snackbar.make(activityLayout, "Comment edited", Snackbar.LENGTH_SHORT).show();
            onRefresh();
        } else
            Snackbar.make(activityLayout, "Comment not edited", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /// FIXME: 27/11/2016 add camera request
        switch(requestCode) {
            case IMAGE_REQUEST_CODE:
                if(resultCode == RESULT_OK)
                    new AsyncTask<String, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(String... params) {
                            JWToken token = Handler.getTokenHandler().getToken(
                                    UserAgent.getInstance(getBaseContext()).getUsername(),
                                    UserAgent.getInstance(getBaseContext()).getPassword()
                            );

                            return token != null && Handler.getCommentHandler().postImage(commentIdforImage, params[0], token);
                        }

                        @Override
                        protected void onPostExecute(Boolean result) {
                            super.onPostExecute(result);

                            if(result) {
                                Snackbar.make(activityLayout, "Image posted", Snackbar.LENGTH_SHORT).show();
                                onRefresh();
                            } else
                                Snackbar.make(activityLayout, "Image not posted", Snackbar.LENGTH_SHORT).show();

                            commentIdforImage = -1;
                        }
                    }.execute(getRealPathFromURI(data.getData()));
                    break;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;

        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private boolean isConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {
            Snackbar.make(activityLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0) {
            if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(activityLayout, "You have to give read permission upload an image", Snackbar.LENGTH_SHORT);
            }
        }
    }

}



