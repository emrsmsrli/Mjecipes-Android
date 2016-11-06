package se.ju.student.android_mjecipes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText email_t_view;
    private EditText password_t_view;
    private Button login_b;
    private CheckBox showPass_cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_t_view = (EditText) findViewById(R.id.login_email);
        password_t_view = (EditText) findViewById(R.id.login_password);
        login_b = (Button) findViewById(R.id.login_button);
        showPass_cb = (CheckBox) findViewById(R.id.login_cb_sp);

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
                    password_t_view.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                else
                    password_t_view.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    private boolean attemptLogin() {
        return true;
    }

}
