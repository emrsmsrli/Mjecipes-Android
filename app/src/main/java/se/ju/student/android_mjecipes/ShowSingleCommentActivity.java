package se.ju.student.android_mjecipes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Comment;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;

public class ShowSingleCommentActivity extends AppCompatActivity {
    String commid;
    TextView t,t1,t2,t3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_single_comment);

        final String scid=getIntent().getStringExtra("cid");
        final String rid=getIntent().getStringExtra("rid");
/*
        Intent i=getIntent();
        commid=i.getStringExtra("commentid");

        t.setText("Comment:"+commid);
        t= (TextView) findViewById(R.id.textView2);
        t1= (TextView) findViewById(R.id.textView3);
        t2= (TextView) findViewById(R.id.textView4);
        t3= (TextView) findViewById(R.id.textView5);


*/
        new AsyncTask<Integer, Void, Comment[]>() {
            @Override
            protected void onPreExecute() {

            }



            @Override
            protected Comment[] doInBackground(Integer... params) {
                return Handler.getRecipeHandler().getComments(Integer.parseInt(rid));
            }


            @Override
            protected void onPostExecute(Comment[] comment) {


                    for(int i=0;i<comment.length;i++){
                        if(comment[i].id==Integer.parseInt(scid)){
                            final View v = ((LinearLayout) findViewById(R.id.activity_show_single_comment2));
                            ((TextView)v.findViewById(R.id.main_single_comment_grade)).setText("Grade= "+Integer.toString(comment[i].grade));

                            ((TextView)v.findViewById(R.id.main_single_comment_commenter)).setText("Commenter= "+comment[i].commenter.userName);
                            ((TextView)v.findViewById(R.id.main_single_comment_text)).setText(comment[i].text);
                            ((TextView)v.findViewById(R.id.main_single_comment_id)).setText(Integer.toString(comment[i].id));

                            if(comment[i].image != null) {
                                final ImageView iv = (ImageView) v.findViewById(R.id.main_single_comment_image);
                                CacheHandler.getImageCacheHandler(getBaseContext()).downloadImage(new ImageRequest(comment[i].image, new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap response) {
                                        iv.setImageBitmap(response);
                                        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    }
                                }, iv.getWidth(), iv.getHeight(), null, null, null));
                            }

                    }

                    }

                Button tab=(Button)findViewById(R.id.tab);

                tab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent comm=new Intent(ShowSingleCommentActivity.this,TabbedActivity.class);

                        startActivity(comm);
                    }
                });


            }
            }.execute(1);

        }
}
