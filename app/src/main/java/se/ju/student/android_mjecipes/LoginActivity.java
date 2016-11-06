package se.ju.student.android_mjecipes;

import android.content.Context;
import android.os.AsyncTask;
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

public class LoginActivity extends AppCompatActivity {

    private EditText email_t_view;
    private EditText password_t_view;
    private Button login_b;
    private CheckBox showPass_cb;
    private View focus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_t_view = (EditText) findViewById(R.id.login_email);
        password_t_view = (EditText) findViewById(R.id.login_password);
        login_b = (Button) findViewById(R.id.login_button);
        showPass_cb = (CheckBox) findViewById(R.id.login_cb_sp);
        focus = null;

        login_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
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

    private void attemptLogin() {
        password_t_view.setError(null);
        email_t_view.setError(null);
        boolean login = true;

        String em = email_t_view.getText().toString();
        String p = password_t_view.getText().toString();

        if(TextUtils.isEmpty(em)) {
            email_t_view.setError("Email can't be empty!");
            focus = email_t_view;
            focus.requestFocus();
            login = false;
        } else if(!emailValid(em)) {
            email_t_view.setError("Email invalid!");
            focus = email_t_view;
            focus.requestFocus();
            login = false;
        }

        if(TextUtils.isEmpty(p)) {
            password_t_view.setError("Password can't be empty!");
            focus = password_t_view;
            focus.requestFocus();
            login = false;
        } else if(passwordValid(p)) {
            password_t_view.setError("Password invalid!");
            focus = password_t_view;
            focus.requestFocus();
            login = false;
        }

        if(login) {
            login(em, p);
        }
    }

    private boolean emailValid(String email) {
        if(email.split("@").length != 2)
            return false;

        return true;
    }

    private boolean passwordValid(String password) {
        if(password.length() < 3 || password.length() > 20)
            return false;

        if(password.toLowerCase().equals(password)
                || password.toUpperCase().equals(password))
            return false;

        return true;
    }

    private void login(String email, String password) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                //TODO implement logic
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                //hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                if(result)
                    Snackbar.make(findViewById(R.id.activity_login), "Logged in!", Snackbar.LENGTH_SHORT).show();
                else
                    Snackbar.make(findViewById(R.id.activity_login), "Error occurred!", Snackbar.LENGTH_SHORT).show();
            }
        }.execute();
    }

}
