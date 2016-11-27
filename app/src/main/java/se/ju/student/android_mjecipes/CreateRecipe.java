package se.ju.student.android_mjecipes;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Account;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Direction;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.TokenHandler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class CreateRecipe extends AppCompatActivity {

        EditText name,direction,description;
        Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        final RelativeLayout l= (RelativeLayout) findViewById(R.id.activity_create_recipe);
        name= (EditText) findViewById(R.id.recipe_name);
        direction=(EditText)findViewById(R.id.recipe_direction);
        description=(EditText)findViewById(R.id.recipe_description);

        b=(Button)findViewById(R.id.button2);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


        final Recipe r=new Recipe();
        r.name=name.getText().toString();
        r.description=description.getText().toString();
                r.directions = new Direction[1];
                r.creatorId=UserAgent.getInstance(getBaseContext()).getUserID();
        r.directions[0]=new Direction();
        r.directions[0].order=1;
        r.directions[0].description= direction.getText().toString();


        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                JWToken token = Handler.getTokenHandler().getToken(UserAgent.getInstance(getBaseContext()).getUsername(),
                        UserAgent.getInstance(getBaseContext()).getPassword());
               Errors e = Handler.getTokenHandler().getErrors();
                if(token==null){
                    return false;
                }
                else
                return Handler.getRecipeHandler().postRecipe(r,token);
            }

            @Override
            protected void onPostExecute(Boolean b) {
                Errors e = Handler.getRecipeHandler().getErrors();
                if(b==null)
                    name.setError("Error");

                if(b==true){
                    Snackbar.make(l, "Recipe Created!!", Snackbar.LENGTH_SHORT).show();
                }


            }
        }.execute();
            }
        });
    }
}
