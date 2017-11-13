package com.mywork.parminder.mapproject;

import android.*;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;

/**
 * Created by Parminder Kaur on 13-09-2017.
 */

public class Signup extends AppCompatActivity {
    Context context;
    Button btn_signin;
    EditText etxt_name,etxt_username,etxt_gender,etxt_emailid,etxt_password;
    String username,password,email,first_name,last_name,gender,imei,bt_name,name,imei2,bt_name2;
    int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);
        btn_signin=(Button)findViewById(R.id.btn_signin);
        etxt_name=(EditText)findViewById(R.id.etxt_name);
        etxt_emailid=(EditText)findViewById(R.id.etxt_emailid);
        etxt_gender=(EditText)findViewById(R.id.etxt_gender);
        etxt_username=(EditText)findViewById(R.id.etxt_username);
        etxt_password=(EditText)findViewById(R.id.etxt_password);
        setImeiAndBluetooth();
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = etxt_username.getText().toString();
                password = etxt_password.getText().toString();
                email = etxt_emailid.getText().toString();
                name = etxt_name.getText().toString();
                gender = etxt_gender.getText().toString();
                //bt_name = bt_name2;
                bt_name = "SNGC_"+username;
                imei = imei2;
                if(name.equals(""))
                {
                    etxt_name.setError("This field can't be empty");
                }
                if(username.equals(""))
                {
                    etxt_username.setError("This field can't be empty");
                }
                if(password.equals(""))
                {
                    etxt_password.setError("This field can't be empty");
                }
                if(email.equals(""))
                {
                    etxt_emailid.setError("This field can't be empty");
                }
                if(gender.equals(""))
                {
                    etxt_gender.setError("This field can't be empty");
                }
                if(!name.equals("") && !username.equals("") && !email.equals("") && !gender.equals("") && !password.equals(""))
                {
                    if (isValidEmail(etxt_emailid.getText().toString().trim()))
                    {
                        if (isValidPassword(etxt_password.getText().toString().trim()))
                        {
                            if(name.contains(" "))
                            {
                                first_name = name.split(" ")[0];
                                last_name = name.split(" ")[1];
                            }
                            else
                            {
                                first_name = name;
                                last_name = name;
                            }
                            sendDataToServer();
                        }
                        else
                            Toast.makeText(Signup.this, "Invalid Password, should contain DIGITS, UPPERCASE, LOWERCASE and SPECIAL characters.", Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(Signup.this, "Invalid Email Id", Toast.LENGTH_SHORT).show();

                }
                else
                    Toast.makeText(Signup.this, "Fill up all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isValidPassword(final String password)
    {
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    public boolean isValidEmail(final String email)
    {
        Pattern pattern;
        Matcher matcher;

        final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private String formatDataAsJSON() {
        final JSONObject root = new JSONObject();
        context = getApplicationContext();
        username = etxt_username.getText().toString();
        password = etxt_password.getText().toString();
        email = etxt_emailid.getText().toString();
        name = etxt_name.getText().toString();
        gender = etxt_gender.getText().toString();
        if(name.contains(" "))
        {
            first_name = name.split(" ")[0];
            last_name = name.split(" ")[1];
        }
        else
        {
            first_name = name;
            last_name = name;
        }
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei2 = tm.getDeviceId();
        bt_name2 = getLocalBluetoothName();
        //bt_name = bt_name2;
        bt_name = "SNGC_"+username;
        imei = imei2;
        try {
            JSONObject user = new JSONObject();
            user.put("username", username);
            user.put("password", password);
            user.put("email", email);
            user.put("first_name", first_name);
            user.put("last_name", last_name);

            root.put("user", user);
            root.put("imei", imei);
            root.put("bt_name", bt_name);
            root.put("gender", gender);

            return root.toString(1);
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
                    try{
                        JSONObject a = new JSONObject();
                        a = getServerData();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
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
                    Toast t = Toast.makeText(getApplicationContext(), "Thank you for Signing up "+username, Toast.LENGTH_SHORT);
                    t.show();
                    Intent intent=new Intent(Signup.this,CurrentLocationActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("username", username);
                    intent.putExtras(extras);
                    startActivity(intent);
                    finish();
                }
                else if (flag == 1)
                {
                    Toast t = Toast.makeText(getApplicationContext(), "The username exists. Please Sign Up again with a different username.", Toast.LENGTH_LONG);
                    t.show();
                    Intent intent=new Intent(Signup.this,Signup.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast t = Toast.makeText(getApplicationContext(), "An unknown error occurred. Please Sign Up again.", Toast.LENGTH_LONG);
                    t.show();
                    Intent intent=new Intent(Signup.this,Signup.class);
                    startActivity(intent);
                    finish();
                }

            }
        }.execute();


    }

    private String getServerResponse(String json) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost("http://18.221.150.85/register/");
        try{
            StringEntity entity = new StringEntity(json);
            post.setEntity(entity);
            post.setHeader("Content-type","application/json");
            DefaultHttpClient client = new DefaultHttpClient();
            BasicResponseHandler handler = new BasicResponseHandler();
            String response = client.execute(post, handler);
            return response;
        } catch (UnsupportedEncodingException e) {
            flag = 2;
            Log.d("JWP", e.toString());
        }catch (ClientProtocolException e) {
            flag = 1;
            Log.d("JWP", e.toString());
        }catch (IOException e) {
            flag = 2;
            Log.d("JWP", e.toString());
        }
        return "Unable to contact Server";
    }

    private JSONObject getServerData() throws Exception
    {
        URL url = new URL("http://18.221.150.85/register/?format=json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        JSONObject result = new JSONObject(response.toString());
        return result;

    }

    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED );
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.READ_PHONE_STATE },1000);
    }

    public void setImeiAndBluetooth()
    {
        if(checkPermission())
        {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imei2 = tm.getDeviceId();
            bt_name2 = getLocalBluetoothName();
        }
        else
            askPermission();
    }

    public String getLocalBluetoothName(){
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        String name = bta.getName();
        if(name == null){
            System.out.println("Name is null!");
            name = bta.getAddress();
        }
        return name;
    }
}

