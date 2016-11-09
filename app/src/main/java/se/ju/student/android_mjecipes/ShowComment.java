package se.ju.student.android_mjecipes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.List;

public class ShowComment extends AppCompatActivity {

    ViewFlipper flipper;

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.activity_show_comment);



        flipper= (ViewFlipper) findViewById(R.id.details);

    }

    public void flip(View v) {
        ListView listcomment= (ListView) findViewById(R.id.commentlist);
        listcomment.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        flipper.showNext();
    }
}
