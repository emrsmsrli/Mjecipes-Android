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
import android.widget.LinearLayout;
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
        if(action.equals(Intent.ACTION_USER_PRESENT))
            showaccount();



        else if(action.equals(Intent.ACTION_EDIT))

            editaccount();

        else if(action.equals(Intent.ACTION_DELETE))
             deleteaccount();




    }


    void deleteaccount(){

        final TextView username,latitude,longitude;
        ImageButton showrecipesbutton,editaccountbutton,deleteaccount;

        setContentView(R.layout.activity_show_account);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username=(TextView) findViewById(R.id.showusername);
        latitude=(TextView) findViewById(R.id.showlatitude);
        longitude=(TextView)  findViewById(R.id.showlongitude);
        editaccountbutton=(ImageButton) findViewById(R.id.editaccount);
        deleteaccount=(ImageButton)findViewById(R.id.deleteaccount);
        final RelativeLayout l= (RelativeLayout) findViewById(R.id.activity_show_account);

        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                JWToken token = Handler.getTokenHandler().getToken(UserAgent.getInstance(getBaseContext()).getUsername(),
                        UserAgent.getInstance(getBaseContext()).getPassword());
                Boolean b=Handler.getAccountHandler().deleteAccount(UserAgent.getInstance(getBaseContext()).getUserID(),token);
                UserAgent.getInstance(getBaseContext()).logout();

                if(b==true) {

                    Intent i=new Intent(getApplicationContext(),MainActivity.class);
                    i.setAction(Intent.ACTION_DELETE);
                    startActivity(i);



                }
                else{
                    Errors e = Handler.getRecipeHandler().getErrors();
                    Snackbar.make(l,e+" Error occured while deleting", Snackbar.LENGTH_SHORT).show();


                }

                return null;
            }
        }.execute();

    }

    void showaccount(){
        final TextView username,latitude,longitude;
        ImageButton showrecipesbutton,editaccountbutton,deleteaccount;

        setContentView(R.layout.activity_show_account);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username=(TextView) findViewById(R.id.showusername);
        latitude=(TextView) findViewById(R.id.showlatitude);
        longitude=(TextView)  findViewById(R.id.showlongitude);
        editaccountbutton=(ImageButton) findViewById(R.id.editaccount);
        deleteaccount=(ImageButton)findViewById(R.id.deleteaccount);


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



        editaccountbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getBaseContext(),ShowAccount.class);
                i.setAction(Intent.ACTION_EDIT);
                startActivity(i);
            }
        });

      deleteaccount.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent i=new Intent(getApplicationContext(),ShowAccount.class);
              i.setAction(Intent.ACTION_DELETE);
              startActivity(i);
              finish();
          }
      });
    }


    void editaccount(){
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
                        return token != null && Handler.getAccountHandler().patchAccount(userid,a,token);
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        if(aBoolean) {
                            Snackbar.make(l, "Account updated", Snackbar.LENGTH_SHORT).show();

                        }
                        else{
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
