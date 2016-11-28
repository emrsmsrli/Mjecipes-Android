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
                latitude=(EditText)findViewById(R.id.lati_tude);
                /*
                if(username.getText().toString().equals(null)||hiddenpassword.getText().toString().equals(null)||latitude.getText().toString().equals(null)||longitude.getText().toString().equals(null)){
                    username.setError("bos");
                    username.requestFocus();
                }
                */

                //else {
                    final Account a = new Account();

                    a.userName = username.getText().toString();
                    a.latitude = Double.parseDouble(latitude.getText().toString());
                    a.longitude = Double.parseDouble(longitude.getText().toString());
                    a.password = hiddenpassword.getText().toString();
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... params) {
                            return Handler.getAccountHandler().postAccount(a);
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            if (aBoolean) {
                                //error yok

                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                //error var
                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_USERNAME_MISSING)) {
                                    username.setError("Username missing");
                                    username.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_DUPLICATE_USERNAME)) {
                                    username.setError("Username exist");
                                    username.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_INVALID_USERNAME)) {
                                    username.setError("Invalid username");
                                    username.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_LATIDUTE_MISSING)) {
                                    longitude.setError("Lantitude missing");
                                    longitude.requestFocus();
                                }


                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_LONGITUDE_MISSING)) {
                                    longitude.setError("Longitude missing");
                                    longitude.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_PASSWORD_MISSING)) {
                                    hiddenpassword.setError("Password missing");
                                    hiddenpassword.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_PASSWORD_REQUIRES_DIGIT)) {
                                    hiddenpassword.setError("Password requires digit");
                                    hiddenpassword.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_PASSWORD_REQUIRES_LOWER)) {
                                    hiddenpassword.setError("Password requires lowercase letter");
                                    hiddenpassword.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_PASSWORD_REQUIRES_NON_ALPHANUM)) {
                                    hiddenpassword.setError("Password requires non alphanum");
                                    hiddenpassword.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_PASSWORD_REQUIRES_UPPER)) {
                                    hiddenpassword.setError("Password requires uppercase letter");
                                    hiddenpassword.requestFocus();
                                }

                                if (Handler.getAccountHandler().getErrors().hasError(Errors.ACCOUNT_PASSWORD_TOO_SHORT)) {
                                    hiddenpassword.setError("Password too short");
                                    hiddenpassword.requestFocus();
                                }




                            }

                        }
                    }.execute();

                //}

            }
        });



    }

}
