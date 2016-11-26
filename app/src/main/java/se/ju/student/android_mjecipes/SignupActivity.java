package se.ju.student.android_mjecipes;




import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
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

        bsign= (Button) findViewById(R.id.bsignin);
        bsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View var) {


                username= (EditText) findViewById(R.id.enterusername);
                hiddenpassword=(EditText)findViewById(R.id.hiddenpassword);
                longitude=(EditText)findViewById(R.id.longi_tude);
                latitude=(EditText)findViewById(R.id.longi_tude);



                final Account a=new Account();

                a.userName=username.getText().toString();
                a.latitude=Double.parseDouble(latitude.getText().toString());
                a.longitude=Double.parseDouble(longitude.getText().toString());
                a.password=hiddenpassword.getText().toString();
                new AsyncTask<Void,Void,Boolean>(){
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        return Handler.getAccountHandler().postAccount(a);
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        if( aBoolean){
                            //error yok

                            Intent i=new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(i);
                        }
                        else{
                            //error var
                           // Handler.getAccountHandler().getErrors().hasError(Errors.)
                            username.setError("Please check your informations");
                            username.requestFocus();
                        }
                    }
                }.execute();



            }
        });


    }

}
