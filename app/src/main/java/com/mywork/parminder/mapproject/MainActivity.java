package com.mywork.parminder.mapproject;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.app.PendingIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    public GoogleApiClient mApiClient;
    Context context;
    String username,password;
    EditText etxt_username,etxt_password;
    Button btn_login;
    TextView txt_signin;
    int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        etxt_username=(EditText)findViewById(R.id.etxt_username);
        etxt_password=(EditText)findViewById(R.id.etxt_password);
        btn_login=(Button)findViewById(R.id.btn_login);
        txt_signin=(TextView)findViewById(R.id.txt_signin);
        boolean a = checkPermission();
        if(!a)
            askPermission();

        txt_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Signup.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = etxt_username.getText().toString();
                password = etxt_password.getText().toString();
                if(username.equals(""))
                {
                    etxt_username.setError("This field can't be empty");
                }
                if(password.equals(""))
                {
                    etxt_password.setError("This field can't be empty");
                }
                if(!username.equals("") && !password.equals("")) {
                    sendDataToServer();
                }
            }
        });
    }

    private String formatDataAsJSON() {
        final JSONObject user = new JSONObject();
        context = getApplicationContext();
        username = etxt_username.getText().toString();
        password = etxt_password.getText().toString();
        try {
            user.put("username", username);
            user.put("password", password);

            return user.toString(1);
        } catch (JSONException e) {
            Log.d("JWP", "Can't format JSON");
        }
        return null;
    }

    private void sendDataToServer() {
        final String json = formatDataAsJSON();
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return getServerResponse(json);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if(flag == 0)
                {
                    Toast t = Toast.makeText(getApplicationContext(), "You are successfully logged in, "+username, Toast.LENGTH_SHORT);
                    t.show();
                    Intent intent=new Intent(MainActivity.this,CurrentLocationActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("username", username);
                    intent.putExtras(extras);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast t = Toast.makeText(getApplicationContext(), "Either username or password is wrong. Try logging in again.", Toast.LENGTH_SHORT);
                    t.show();
                    Intent intent=new Intent(MainActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }.execute();

    }

    private String getServerResponse(String json) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost("http://18.221.150.85/login/");
        try{
            StringEntity entity = new StringEntity(json);
            post.setEntity(entity);
            post.setHeader("Content-type","application/json");
            DefaultHttpClient client = new DefaultHttpClient();
            BasicResponseHandler handler = new BasicResponseHandler();
            String response = client.execute(post, handler);
            return response;
        } catch (UnsupportedEncodingException e) {
            flag = 1;
            Log.d("JWP", e.toString());
        }catch (ClientProtocolException e) {
            flag = 1;
            Log.d("JWP", e.toString());
        }catch (IOException e) {
            flag = 1;
            Log.d("JWP", e.toString());
        }catch (Exception e) {
            flag = 1;
            Log.d("JWP", e.toString());
        }
        return "Unable to contact Server";
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognitionService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 3000, pendingIntent );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED );
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] { android.Manifest.permission.READ_PHONE_STATE },1000);
    }

}

