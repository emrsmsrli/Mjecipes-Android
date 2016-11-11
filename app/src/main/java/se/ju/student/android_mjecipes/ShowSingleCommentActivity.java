package se.ju.student.android_mjecipes;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;

public class ShowSingleCommentActivity extends AppCompatActivity {
    String commid;
    TextView t,t1,t2,t3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_single_comment);

        Intent i=getIntent();
        commid=i.getStringExtra("commentid");

        t.setText("Comment:"+commid);
        t= (TextView) findViewById(R.id.textView2);
        t1= (TextView) findViewById(R.id.textView3);
        t2= (TextView) findViewById(R.id.textView4);
        t3= (TextView) findViewById(R.id.textView5);



        new AsyncTask<Integer, Void, Comment[]>() {
            @Override
            protected void onPreExecute() {

            }



            @Override
            protected Comment[] doInBackground(Integer... params) {
                return Handler.getRecipeHandler().getComments(Integer.parseInt(commid));
            }


            @Override
            protected void onPostExecute(Comment[] comment) {

/*
                t1.setText("Grade:"+Integer.toString(comment[Integer.parseInt(commid)].grade));
                t.setText("Comment:"+comment[Integer.parseInt(commid)].text);
                t2.setText("Commenter:"+comment[Integer.parseInt(commid)].commenter.userName);
                t3.setText("Image:"+comment[Integer.parseInt(commid)].image);
*/
            }
        }.execute(1);
    }
}
