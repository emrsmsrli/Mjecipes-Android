package se.ju.student.android_mjecipes;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.Account;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities.JWToken;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Errors;
import se.ju.student.android_mjecipes.MjepicesAPIHandler.Handler;
import se.ju.student.android_mjecipes.UserAgent.UserAgent;

public class ShowAccount extends AppCompatActivity {


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i=this.getIntent();
        String action= i.getAction();
        if(action==Intent.ACTION_USER_PRESENT) {

            final TextView username,latitude,longitude;
            ImageButton showrecipesbutton,editaccountbutton;

        setContentView(R.layout.activity_show_account);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username=(TextView) findViewById(R.id.showusername);
        latitude=(TextView) findViewById(R.id.showlatitude);
        longitude=(TextView)  findViewById(R.id.showlongitude);
        showrecipesbutton=(ImageButton) findViewById(R.id.imageButton);
        editaccountbutton=(ImageButton) findViewById(R.id.editaccount);


        new AsyncTask<Void,Void,Account>(){
            @Override
            protected Account doInBackground(Void... params) {
                Boolean b= UserAgent.getInstance(getBaseContext()).isLoggedIn();
                String userid=UserAgent.getInstance(getBaseContext()).getUserID();
                Account a=Handler.getAccountHandler().getAccount(userid);
                if(b==true)
                    return a;

                else return null;
            }

            @Override
            protected void onPostExecute(Account account) {
                if(account!=null){
                    username.setText(account.userName);
                    latitude.setText(Double.toString(account.latitude));
                    longitude.setText(Double.toString(account.longitude));
                }


            }
        }.execute();



        showrecipesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getBaseContext(),MainActivity.class);
                startActivity(i);

            }
        });



        editaccountbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getBaseContext(),ShowAccount.class);
                i.setAction(Intent.ACTION_EDIT);
                startActivity(i);
            }
        });

    }

    else if(action==Intent.ACTION_EDIT){

            setContentView(R.layout.edit_account);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            final EditText longitude,latitude;
            final RelativeLayout l= (RelativeLayout) findViewById(R.id.editaccount);

            longitude=(EditText)findViewById(R.id.showlongitude_edit);
            latitude=(EditText )findViewById(R.id.showlatitude_edit);


            final Button b;

            b=(Button) findViewById(R.id.buttonsave);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



            final Account a=new Account();

            if(longitude!=null)
                a.longitude=Double.parseDouble(longitude.getText().toString());
            if(latitude!=null)
                a.latitude=Double.parseDouble(latitude.getText().toString());

            new AsyncTask<Void,Void,Boolean>(){
                @Override
                protected Boolean doInBackground(Void... params) {

                    String userid=UserAgent.getInstance(getBaseContext()).getUserID();
                    JWToken token = Handler.getTokenHandler().getToken(UserAgent.getInstance(getBaseContext()).getUsername(),
                            UserAgent.getInstance(getBaseContext()).getPassword());
                    Boolean b=Handler.getAccountHandler().patchAccount(userid,a,token);
                    return b;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    final Boolean check=aBoolean;
                    if(aBoolean==true) {
                        Snackbar.make(l, "Account updated", Snackbar.LENGTH_SHORT).show();

                    }
                    else{
                        Errors e = Handler.getRecipeHandler().getErrors();
                       latitude.setError("Error");
                    }






                }
            }.execute();


                        Intent i = new Intent(getBaseContext(), ShowAccount.class);
                        i.setAction(Intent.ACTION_USER_PRESENT);
                        startActivity(i);






                }
            });




        }



    }
}
