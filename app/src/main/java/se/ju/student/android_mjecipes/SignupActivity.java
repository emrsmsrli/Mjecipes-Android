package se.ju.student.android_mjecipes;




import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.ju.student.android_mjecipes.CacheHandlers.CacheHandler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Recipe;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Account;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class SignupActivity extends AppCompatActivity {

    EditText username;
    EditText hiddenpassword;
    EditText hiddenpasswordcheck;
    EditText latitude;
    EditText longitude;
    TextView tvResults;
    Button bsign;
    RelativeLayout relativeLayout;
    private boolean hasLocation = true;
    CheckBox checkBox ;
    final Account a = new Account();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign__in);
        relativeLayout= (RelativeLayout) findViewById(R.id.sign_up);
        bsign= (Button) findViewById(R.id.bsignin);

        a.latitude = 0.0;
        a.longitude = 0.0;
        checkBox=(CheckBox) findViewById(R.id.checkBox);

        if(!(isConnectionAvailable())){
                Snackbar.make(relativeLayout, "No internet connection. You can't create an account.", Snackbar.LENGTH_LONG).show();
                bsign.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View var) {Snackbar.make(relativeLayout, "No internet connection!", Snackbar.LENGTH_LONG).show();
                    }
                });

            }else{

            signup();
            }




    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
    }

    private void setLocation(final Account a) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }

        Criteria criteria = new Criteria();
        criteria.setAltitudeRequired(false);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String providerName = locationManager.getBestProvider(criteria, false);

        Location location;
        if(locationManager.isProviderEnabled(providerName))
            location = locationManager.getLastKnownLocation(providerName);
        else {
            Toast.makeText(this, "Enable location providers", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        if(location == null) {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateLocation(location,a);
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            };

            locationManager.requestSingleUpdate(providerName, locationListener, Looper.myLooper());
        } else
            updateLocation(location,a);
    }

    private void updateLocation(Location location,Account a) {

        a.latitude = location.getLatitude();
        a.longitude = location.getLongitude();


    }


    public void selectitem(View view){
        boolean checked=((CheckBox) view).isChecked();

        switch (view.getId()){
            case R.id.checkBox:
                if(checked)
                    setLocation(a);
            break;

            default:
                a.latitude = 0.0;
                a.longitude = 0.0;
        }
    }

    void signup(){

        bsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View var) {
                var.setClickable(true);

                username = (EditText) findViewById(R.id.enterusername);
                hiddenpassword = (EditText) findViewById(R.id.hiddenpassword);

                hiddenpasswordcheck = (EditText) findViewById(R.id.hiddenpasswordcorrection);

                a.userName = username.getText().toString();

                a.password = hiddenpassword.getText().toString();


                if (hiddenpassword.getText().toString().equals(hiddenpasswordcheck.getText().toString())) {

                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... params) {
                            Errors e = Handler.getTokenHandler().getErrors();
                            return Handler.getAccountHandler().postAccount(a);
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            Errors e = new Errors();
                            checkerror(aBoolean);
                        }
                    }.execute();

                    //}

                }
                else{
                    bsign.setClickable(true);
                    hiddenpassword.setError("Passwords are not equal");
                    hiddenpassword.requestFocus();
                }
            }
        });

    }




    void checkerror(Boolean aBoolean){
        if (aBoolean) {
            //error yok

            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            bsign.setClickable(true);

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
                latitude.setError("Latitude missing");
                latitude.requestFocus();
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





    private boolean isConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni !=null && ni.isConnected();
    }

}
