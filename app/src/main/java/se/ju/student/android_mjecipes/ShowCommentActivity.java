package se.ju.student.android_mjecipes;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout activityLayout;
    private boolean loaded = false;
    private boolean imgloaded = false;
    private String recipeIDExtra;
    private ActionMode actionMode;
    private CreateCommentFragment fragment;

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

        new AsyncTask<String, Void, Comment[]>() {

            @Override
            protected Comment[] doInBackground(String... p) {
                Comment[] comments;
                if(!loaded) {
                    comments = CacheHandler.getJSONJsonCacheHandler(getBaseContext()).readCommentsOfRecipe(p[0]);

                    if(comments != null) {
                        loaded = true;
                        return comments;
                    }
                }

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni == null || !ni.isConnected()) {
                    Snackbar.make(activityLayout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
                    return null;
                }

                comments = Handler.getRecipeHandler().getComments(Integer.parseInt(p[0]));
                if(comments != null) {
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
                                    if(UserAgent.getInstance(getBaseContext()).isLoggedIn() &&
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
                                            new AsyncTask<Void, Void, Void>() {
                                                @Override
                                                protected Void doInBackground(Void... params) {
                                                    JWToken token = Handler.getTokenHandler().getToken(
                                                            UserAgent.getInstance(getBaseContext()).getUsername(),
                                                            UserAgent.getInstance(getBaseContext()).getPassword()
                                                    );

                                                    if(token == null) return null;

                                                    Handler.getCommentHandler().deleteComment(Integer.parseInt(commentid), token);
                                                    CacheHandler.getJSONJsonCacheHandler(getBaseContext()).clearSingleJSONCache(commentid, Comment.class);
                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Void aVoid) {
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
                                            //TODO comment image upload
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
}



