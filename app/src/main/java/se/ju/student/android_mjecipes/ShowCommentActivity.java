package se.ju.student.android_mjecipes;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;


public class ShowCommentActivity extends AppCompatActivity {

    int i;
    LinearLayout r;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_show_comment);
        r = (LinearLayout) findViewById(R.id.commentsection);


        new AsyncTask<Integer, Void, Comment[]>() {
            protected void onPreExecute() {

            }

            protected Comment[] doInBackground(Integer... p) {
                return Handler.getRecipeHandler().getComments(p[0]);
            }

            protected void onPostExecute(final Comment[] comments) {
                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                for( i=0;i<comments.length;++i){
                    inf.inflate(R.layout.comment_list, r);
                    TextView t = (TextView)r.getChildAt(i);
                    t.setText(Integer.toString(comments[i].id));






                }

            }
        }.execute(1);




        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent c= new Intent(ShowCommentActivity.this,ShowSingleCommentActivity.class);
                int a=v.getId();
                TextView t = (TextView)r.getChildAt(0);
                c.putExtra("commentid",t.getText());
                startActivity(c);
            }
        });



    }
}



