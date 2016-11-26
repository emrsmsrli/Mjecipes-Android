package se.ju.student.android_mjecipes;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;


public class ShowCommentActivity extends AppCompatActivity {

    int i;
    LinearLayout r;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_show_comment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final String rid=getIntent().getStringExtra("resid");

        new AsyncTask<Integer, Void, Comment[]>() {
            protected void onPreExecute() {

            }

            protected Comment[] doInBackground(Integer... p) {

                return Handler.getRecipeHandler().getComments(Integer.parseInt(rid));

            }

            protected void onPostExecute(final Comment[] comments) {
                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    for (i = 0; i < comments.length; ++i) {
                        inf.inflate(R.layout.comment_list, (LinearLayout) findViewById(R.id.activity_show_comment));
                        final View v = ((LinearLayout) findViewById(R.id.activity_show_comment)).getChildAt(i);

                        ((TextView)v.findViewById(R.id.main_comment_id)).setText(Integer.toString(comments[i].id));
                        ((TextView)v.findViewById(R.id.main_comment_grade)).setText("Grade= "+Integer.toString(comments[i].grade));

                        ((TextView)v.findViewById(R.id.main_comment_text)).setText(comments[i].text);
                        ((TextView)v.findViewById(R.id.main_comment_commenter)).setText("Commenter= " +(CharSequence) comments[i].commenter.userName);
                    }

            }
        }.execute(1);
    }
}



