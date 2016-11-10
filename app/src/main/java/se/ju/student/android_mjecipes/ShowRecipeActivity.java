package se.ju.student.android_mjecipes;
import android.view.View;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import se.ju.student.android_mjecipes.APIHandler.Handler;
import se.ju.student.android_mjecipes.Entities.Recipe;

public class ShowRecipeActivity extends AppCompatActivity {


    LinearLayout r;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);
        r = (LinearLayout) findViewById(R.id.recipesection);

        new AsyncTask<Integer,Void,Recipe[]>(){
            @Override
            protected void onPreExecute() {

            }
            protected Recipe[] doInBackground(Integer... p){
                return Handler.getRecipeHandler().getRecipeByPage(p[0]);
            }

            @Override
            protected void onPostExecute(Recipe[] recipes) {
                LayoutInflater inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    for(int i=0;i<recipes.length;i++){
                        inf.inflate(R.layout.recipe_list,r);
                        TextView t = (TextView)r.getChildAt(i);
                        t.setText(recipes[i].name);
                    }
            }
        }.execute(1);

    }
}
