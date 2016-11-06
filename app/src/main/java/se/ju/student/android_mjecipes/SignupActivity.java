package se.ju.student.android_mjecipes;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SignupActivity extends AppCompatActivity {

    EditText username;
    EditText hiddenpassword;
    TextView tvResults;
    Button bsign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign__in);


        username=(EditText) findViewById(R.id.enterusername);
        hiddenpassword=(EditText) findViewById(R.id.hiddenpassword);
        tvResults=(TextView) findViewById(R.id.inputs);
        bsign= (Button) findViewById(R.id.bsignin);
        bsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View var) {
                Account a=new Account();


                a.username=username.getText().toString();
                a.password=hiddenpassword.getText().toString();

                tvResults.setText(a.username+" "+a.password);

                JSONObject jsonObject= new JSONObject();
                try {
                    jsonObject.put("UserName",a.username);
                    jsonObject.put("Password",a.password);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });



    }

}
