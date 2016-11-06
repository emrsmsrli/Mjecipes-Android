package se.ju.student.android_mjecipes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowRecipe extends AppCompatActivity {

    ImageView recipeImage;
    TextView recipeName;
    TextView recipeDesc;

    Button comments;
    Button directions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);

        recipeImage = (ImageView) findViewById(R.id.show_recipe_img);
        recipeName = (TextView) findViewById(R.id.show_recipe_name);
        recipeDesc = (TextView) findViewById(R.id.show_recipe_desc);

        directions = (Button) findViewById(R.id.show_recipes_b_directions);
        comments = (Button) findViewById(R.id.show_recipes_b_comments);

        directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO intent directions activity
            }
        });
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO intent comments activity
            }
        });
    }
}
