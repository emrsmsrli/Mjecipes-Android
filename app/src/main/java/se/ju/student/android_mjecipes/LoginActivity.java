package se.ju.student.android_mjecipes;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.support.design.widget.Snackbar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class LoginActivity extends AppCompatActivity {

    private EditText email_t_view;
    private EditText password_t_view;
    private Button login_b;
    private CheckBox showPass_cb;
    private View focus;
    RelativeLayout relativeLayout;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        relativeLayout=(RelativeLayout) findViewById(R.id.log_in);
        email_t_view = (EditText) findViewById(R.id.login_email);
        password_t_view = (EditText) findViewById(R.id.login_password);
        login_b = (Button) findViewById(R.id.login_button);
        showPass_cb = (CheckBox) findViewById(R.id.login_cb_sp);
        focus = null;


        if(!(isConnectionAvailable())){

            Snackbar.make(relativeLayout, "No internet connection. You can't login.", Snackbar.LENGTH_LONG).show();
            login_b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(relativeLayout, "No internet connection!", Snackbar.LENGTH_LONG).show();
                }
            });
        }else{
            login();
        }



    }

    void login(){
        login_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                UserAgent.getInstance(getBaseContext()).login(email_t_view.getText().toString(), password_t_view.getText().toString(), new UserAgent.LoginListener() {
                    @Override
                    public void onLogin(boolean loggedIn) {
                        if(loggedIn){
                            Intent i=new Intent(getApplicationContext(),MainActivity.class);
                            i.setAction(Intent.ACTION_MAIN);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        }
                        else{
                            login_b.setClickable(true);
                            email_t_view .setError("Password or Username wrong!");
                            email_t_view .requestFocus();
                        }
                    }
                });
            }
        });

        showPass_cb.setChecked(false);
        showPass_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    password_t_view.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                else
                    password_t_view.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }
    private boolean isConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni !=null && ni.isConnected();
    }
}
