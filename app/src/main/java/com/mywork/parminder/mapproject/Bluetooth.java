package com.mywork.parminder.mapproject;

import android.app.Activity;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.Timer;

/**
 * Created by HP on 31-Oct-17.
 */

public class Bluetooth extends Activity{
    private static final String LOG="TAg" ;
    boolean doubleBackToExitPressedOnce = false;
    Location location;
    String lati, longi;
    private int interval = 60000;
    private Handler mHandler;
    int flag = 0;
    Context context;
    Button b1,b2,b3,b4,enter,locate;
    TextView tv1,tv2,tv3,tv4;
    Integer username1,username2;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ArrayList<String> bt_devices = new ArrayList<String>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<Integer> rss = new ArrayList<Integer>();
    ArrayList<String> u_names = new ArrayList<String>();
    ArrayList<Integer> u_rss = new ArrayList<Integer>();
    ArrayList<Integer> rating = new ArrayList<Integer>();
    ListView lv;

    private OutputStream outputStream;
    private InputStream inStream;
    public String b_name,user;
    String date, time;

    Handler handlePush = new Handler();

    Runnable pushData = new Runnable() {
        @Override
        public void run() {
            if(u_names.isEmpty())
            {
                if(!BA.isEnabled())
                    Toast.makeText(getApplicationContext(), "Please first turn ON your bluetooth, get VISIBLE, and REFRESH",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Sorry, no data to send to the server",Toast.LENGTH_SHORT).show();
            }
            else {
                sendDataToServer();
            }
            handlePush.postDelayed(pushData, 10000);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        BA = BluetoothAdapter.getDefaultAdapter();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        final String username = extras.getString("username","");
        final String latitude = extras.getString("latitude","");
        final String longitude = extras.getString("longitude","");
        lati = latitude;
        longi = longitude;
        user = username;
        b1 = (Button) findViewById(R.id.button);
        b2=(Button)findViewById(R.id.button2);
        b3=(Button)findViewById(R.id.button3);
        b4=(Button)findViewById(R.id.button4);
        locate=(Button)findViewById(R.id.button7);
        tv1 = (TextView) findViewById(R.id.textView3) ;
        tv2 = (TextView) findViewById(R.id.textView4) ;
        tv3 = (TextView) findViewById(R.id.textView5) ;
        tv4 = (TextView) findViewById(R.id.textView6) ;
        Calendar calendar = Calendar.getInstance();
        date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a");
        time = simpleDateFormat.format(calendar.getTime());
        enter=(Button)findViewById(R.id.enter);
        tv1.setText("Lat: "+ latitude);
        tv2.setText("Long: "+longitude);
        tv3.setText(date);
        tv4.setText(time);
        lv = (ListView)findViewById(R.id.listView);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(u_names.isEmpty())
                {
                    if(!BA.isEnabled())
                        Toast.makeText(getApplicationContext(), "Please first turn ON your bluetooth, get VISIBLE, and REFRESH",Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Sorry, no data to send to the server",Toast.LENGTH_SHORT).show();
                }
                else
                    sendDataToServer();
            }
        });
        handlePush.postDelayed(pushData, 500);
    }

    public void on(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            b_name=BA.getName();
            BA.setName("SNGC_"+user);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_SHORT).show();
        }
    }

    public void off(View v){
        if (!BA.isEnabled())
            Toast.makeText(getApplicationContext(), "Already off", Toast.LENGTH_SHORT).show();
        else
        {
            BA.setName(b_name);
            BA.disable();
            Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_SHORT).show();
        }
    }


    public  void visible(View v){
        if (!BA.isEnabled())
            Toast.makeText(getApplicationContext(), "First, turn bluetooth ON", Toast.LENGTH_SHORT).show();
        else
        {
            if(!BA.getName().contains("SNGC_"))
                BA.setName("SNGC_"+user);
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(getVisible, 0);
        }
    }

    public void locate(View v) {
        Intent intent=new Intent(Bluetooth.this,CurrentLocationActivity.class);
        Bundle extras = new Bundle();
        extras.putString("username", user);
        intent.putExtras(extras);
        startActivity(intent);
        finish();
    }

    public void show(View v)
    {
        if (!BA.isEnabled())
            Toast.makeText(getApplicationContext(), "First, turn bluetooth ON and REFRESH", Toast.LENGTH_SHORT).show();
        else
        {
            ArrayList list = new ArrayList();
            for(String name: names)
            {
                if(name.contains("SNGC_"))
                {
                    int pos = names.indexOf(name);
                    String strength = rss.get(pos).toString();
                    if(!list.contains(name+": "+strength))
                    {
                        u_names.add(name.split("_")[1]);
                        u_rss.add(rss.get(pos));
                        list.add(name+": "+strength);
                    }
                }
            }
            if(list.isEmpty())
            {
                Toast.makeText(getApplicationContext(), "Sorry, No group members found!",Toast.LENGTH_SHORT).show();
            }
            else
            {
                final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
                lv.setAdapter(adapter);
            }
        }

    }

    public void list(View v){
        if (!BA.isEnabled())
            Toast.makeText(getApplicationContext(), "First, turn bluetooth ON and GET VISIBLE", Toast.LENGTH_SHORT).show();
        else
        {
            if(!BA.getName().contains("SNGC_"))
                BA.setName("SNGC_"+user);
            Toast.makeText(getApplicationContext(), "Please wait while the devices are being gathered...",Toast.LENGTH_SHORT).show();
            BA.startDiscovery();
            pairedDevices = BA.getBondedDevices();
            final String[] str = {""};
            ArrayList list = new ArrayList();
            for(String name: names)
            {
                if(name.contains("SNGC_"))
                {
                    int pos = names.indexOf(name);
                    String strength = rss.get(pos).toString();
                    if(!list.contains(name+": "+strength))
                    {
                        list.add(name+": "+strength);
                    }
                }
            }
        }
        //Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
        /*final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);*/
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                String bt = name+": "+rssi;
                if(!names.contains(name))
                {
                    bt_devices.add(bt);
                    names.add(name);
                    rss.add(rssi);
                    Toast.makeText(getApplicationContext(), bt, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void init() throws IOException {
        if (BA != null) {
            if (BA.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = BA.getBondedDevices();
                int position=0;
                if(bondedDevices.size() > 0) {
                    Object[] devices = (Object []) bondedDevices.toArray();
                    BluetoothDevice device = (BluetoothDevice) devices[position];
                    ParcelUuid[] uuids = device.getUuids();
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    inStream = socket.getInputStream();
                }

                Log.e("error", "No appropriate paired devices.");
            } else {
                Log.e("error", "Bluetooth is disabled.");
            }
        }
    }
    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }
    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;
        int b = BUFFER_SIZE;

        while (true) {
            try {
                bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatDataAsJSON() {
        final JSONObject root = new JSONObject();
        context = getApplicationContext();
        int size = u_rss.size();
        ArrayList<Integer> u_rss2 = new ArrayList<Integer>();
        Collections.sort(u_rss2);
        for(Integer r: u_rss)
        {
            if(r >= -60)
                rating.add(5);
            else if(r >= -70)
                rating.add(4);
            else if(r >= -80)
                rating.add(3);
            else if(r >= -90)
                rating.add(2);
            else if(r >= -100)
                rating.add(1);
            else
                rating.add(0);
        }
        /*username1 = 2;
        username2 = 3;*/
        try {
            JSONObject location2 = new JSONObject();
            location2.put("lat", lati);
            location2.put("long", longi);

            JSONObject group = new JSONObject();
            for(int i=0; i<size; i++)
            {
                group.put(u_names.get(i), rating.get(i));
            }
            /*group.put("username1", username1);
            group.put("username2", username2);*/

            JSONArray username = new JSONArray();
            JSONObject val1 = new JSONObject();
            //JSONObject val2 = new JSONObject();

            val1.put("date", date);
            val1.put("time", time);
            val1.put("location", location2);
            val1.put("group", group);

            /*val2.put("date", date);
            val2.put("time", time);
            val2.put("location", location2);
            val2.put("group", group);*/

            username.put(val1);
            //username.put(val2);

            root.put(user, username);
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
                    String a = getServerResponse(json);
                    return a;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (flag == 0)
                {
                    Toast.makeText(getApplicationContext(), "Data sent to server", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "ERROR...", Toast.LENGTH_SHORT).show();
                }

            }
        }.execute();
    }

    private String getServerResponse(String json) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost("http://18.221.150.85/groups/");
        try{
            StringEntity entity = new StringEntity(json);
            post.setEntity(entity);
            post.setHeader("Content-type","application/json");
            DefaultHttpClient client = new DefaultHttpClient();
            BasicResponseHandler handler = new BasicResponseHandler();
            String response = client.execute(post, handler);
            return response;
        } catch (UnsupportedEncodingException e) {
            Log.d("JWP", e.toString());
            flag = 1;
        }catch (ClientProtocolException e) {
            Log.d("JWP", e.toString());
            flag = 1;
        }catch (IOException e) {
            Log.d("JWP", e.toString());
            flag = 1;
        }catch (Exception e) {
            Log.d("JWP", e.toString());
            flag = 1;
        }
        return "Unable to contact Server";
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
