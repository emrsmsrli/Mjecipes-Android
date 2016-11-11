package se.ju.student.android_mjecipes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;

public class CreateRecipe extends AppCompatActivity {

        EditText name,direction,description;
        Button createrecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);



        name=(EditText) findViewById(R.id.recipenamefield);
        direction=(EditText) findViewById(R.id.directionfield);
        description= (EditText) findViewById(R.id.descriptionfield);

        createrecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Recipe r=new Recipe();


            }
        });
    }
}
