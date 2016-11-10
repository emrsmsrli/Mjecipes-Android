package se.ju.student.android_mjecipes;

import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.List;

import se.ju.student.android_mjecipes.APIHandler.Handler;
import se.ju.student.android_mjecipes.Entities.Comment;
import se.ju.student.android_mjecipes.Entities.Recipe;

public class ShowCommentActivity extends AppCompatActivity {


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

            protected void onPostExecute(Comment[] comments) {
                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                for(int i=0;i<comments.length;++i){
                    inf.inflate(R.layout.comment_list, r);
                    TextView t = (TextView)r.getChildAt(i);
                    t.setText(comments[i].text);
                }

            }
        }.execute(1);
    }
}



