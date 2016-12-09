package se.ju.student.android_mjecipes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Account;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class ShowAccountActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;

    private String accountId;
    private boolean hasLocation = true;

    private RelativeLayout activityLayout;
    private TextView usernameTV;
    private TextView latitudeTV;
    private TextView longitudeTV;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_account);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setHomeButtonEnabled(true);

        activityLayout = (RelativeLayout) findViewById(R.id.activity_show_account);
        usernameTV = (TextView) findViewById(R.id.showusername);
        latitudeTV = (TextView) findViewById(R.id.showlatitude);
        longitudeTV = (TextView) findViewById(R.id.showlongitude);

        accountId = getIntent().getStringExtra("aID");

        if(accountId == null)
            accountId = UserAgent.getInstance(this).getUserID();

        getAccount();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(isConnectionAvailable() && UserAgent.getInstance(this).getUserID().equals(accountId)) {
            menu.findItem(R.id.delete_account).setVisible(true);
            if(hasLocation) {
                menu.findItem(R.id.edit_location).setVisible(true);
                menu.findItem(R.id.delete_location).setVisible(true);
            } else
                menu.findItem(R.id.add_location).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_account_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!isConnectionAvailable()) {
            Snackbar.make(activityLayout, R.string.no_connection, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        switch(item.getItemId()) {
            case R.id.add_location:
            case R.id.edit_location:
                setLocation();
                break;
            case R.id.delete_location:
                deleteLocation();
                break;
            case R.id.delete_account:
                deleteAccount();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void getAccount() {
        new AsyncTask<String, Void, Account>() {
            @Override
            protected Account doInBackground(String... params) {
                if(isConnectionAvailable()) {
                    return Handler.getAccountHandler().getAccount(params[0]);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Account account) {
                if(account == null) {
                    Snackbar.make(activityLayout, R.string.no_connection, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(account.latitude == 0 && account.longitude == 0)
                    hasLocation = false;

                invalidateOptionsMenu();

                usernameTV.setText(account.userName);
                latitudeTV.setText(String.format(Locale.ENGLISH, "%f", account.latitude));
                longitudeTV.setText(String.format(Locale.ENGLISH, "%f", account.longitude));
            }
        }.execute(accountId);
    }

    private void deleteAccount(){
        new AsyncTask<String, Void, Boolean>(){
            @Override
            protected void onPostExecute(Boolean result) {
                if(result) {
                    Snackbar.make(activityLayout, R.string.done, Snackbar.LENGTH_SHORT).show();
                    UserAgent.getInstance(getBaseContext()).logout();
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, Snackbar.LENGTH_SHORT);
                } else {
                    Snackbar.make(activityLayout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            protected Boolean doInBackground(String... params) {
                JWToken token = Handler.getTokenHandler().getToken(
                        UserAgent.getInstance(ShowAccountActivity.this).getUsername(),
                        UserAgent.getInstance(ShowAccountActivity.this).getPassword()
                );

                return token != null && Handler.getAccountHandler().deleteAccount(UserAgent.getInstance(getBaseContext()).getUserID(), token);
            }
        }.execute(accountId);

    }

    private void setLocation() {
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
                    updateLocation(location);
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
            updateLocation(location);
    }

    private void updateLocation(Location location) {
        Account account = new Account();
        account.latitude = location.getLatitude();
        account.longitude = location.getLongitude();

        new AsyncTask<Account, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Account... params) {
                JWToken token = Handler.getTokenHandler().getToken(
                        UserAgent.getInstance(ShowAccountActivity.this).getUsername(),
                        UserAgent.getInstance(ShowAccountActivity.this).getPassword()
                );

                return token != null && Handler.getAccountHandler().patchAccount(
                        accountId,
                        params[0],
                        token
                );
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result) {
                    Snackbar.make(activityLayout, R.string.done, Snackbar.LENGTH_SHORT).show();

                    hasLocation = true;
                    getAccount();
                } else
                    Snackbar.make(activityLayout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
            }
        }.execute(account);
    }

    private void deleteLocation() {
        Account account = new Account();
        account.latitude = 0.0;
        account.longitude = 0.0;

        new AsyncTask<Account, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Account... params) {
                JWToken token = Handler.getTokenHandler().getToken(
                        UserAgent.getInstance(ShowAccountActivity.this).getUsername(),
                        UserAgent.getInstance(ShowAccountActivity.this).getPassword()
                );

                return token != null && Handler.getAccountHandler().patchAccount(
                        accountId,
                        params[0],
                        token
                );
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result) {
                    Snackbar.make(activityLayout, R.string.done, Snackbar.LENGTH_SHORT).show();

                    hasLocation = false;
                    getAccount();
                } else
                    Snackbar.make(activityLayout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
            }
        }.execute(account);
    }

    private boolean isConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni !=null && ni.isConnected();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Snackbar.make(activityLayout, R.string.error_permission_needed, Snackbar.LENGTH_SHORT).show();
                else setLocation();
                break;
        }
    }
}
