package se.ju.student.android_mjecipes;




import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Account;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class SignupActivity extends AppCompatActivity {

    EditText username;
    EditText hiddenpassword;
    EditText latitude;
    EditText longitude;
    TextView tvResults;
    Button bsign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign__in);


        username=(EditText) findViewById(R.id.enterusername);
        hiddenpassword=(EditText) findViewById(R.id.hiddenpassword);
        latitude= (EditText) findViewById(R.id.lati_tude);
        longitude= (EditText) findViewById(R.id.longi_tude);
        tvResults=(TextView) findViewById(R.id.inputs);
        bsign= (Button) findViewById(R.id.bsignin);
        bsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View var) {

                Account a=new Account();

                        a.userName=username.getText().toString();
                        a.latitude=Double.parseDouble(latitude.getText().toString());
                        a.longitude=Double.parseDouble(longitude.getText().toString());
                        a.password=hiddenpassword.getText().toString();


            }
        });



    }

}
