package se.ju.student.android_mjecipes;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowRecipeActivity extends AppCompatActivity {

    ImageView recipeImage;
    TextView recipeDesc;

    LinearLayout directions;

    Button comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);

        recipeImage = (ImageView) findViewById(R.id.show_recipe_img);
        recipeDesc = (TextView) findViewById(R.id.show_recipe_desc);
        comments = (Button) findViewById(R.id.show_recipes_b_comments);
        directions = (LinearLayout) findViewById(R.id.show_recipes_ll_directions);

        for(int i = 0; i < 30; ++i) {
            TextView tv = new TextView(getBaseContext());
            tv.setText("sjkfhaşskjfsa");
            tv.setPadding(0,0,0,10);
            tv.setTextColor(Color.rgb(128,128,128));
            directions.addView(tv);
            setTitle("asdasd"); //TODO set recipe name
        }

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO intent comments activity
            }
        });
    }
}
