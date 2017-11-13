package com.mywork.parminder.mapproject;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.mywork.parminder.mapproject.R.id.map;

public class CurrentLocationActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, View.OnClickListener,

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {
    boolean doubleBackToExitPressedOnce = false;
    private static final String TAG ="" ;
    Location floc;
    Double latitude,longitude;
    Context context;
    Button btn_bluetooth;
    String user;
    String date, time; Integer username1,username2,username3, username4, username5;
  ArrayList<LatLng> arrayList=new ArrayList<>();
    //private static final String TAG = MainActivity.class.getSimpleName();

    TextView textLat, textLong;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    Location lastLocation;
    private Marker locationMarker;
    private final int UPDATE_INTERVAL =  3 * 60 * 1000; // 3 minutes
    private final int FASTEST_INTERVAL = 30 * 1000;  // 30 secs
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        final String username = extras.getString("username","");
        user = username;
        textLat=(TextView)findViewById(R.id.txt_lat);
        textLong=(TextView)findViewById(R.id.txt_long);
        btn_bluetooth=(Button)findViewById(R.id.button5);

        btn_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CurrentLocationActivity.this,Bluetooth.class);
                Bundle extras = new Bundle();
                extras.putString("username", user);
                extras.putString("latitude", ""+floc.getLatitude());
                extras.putString("longitude", ""+floc.getLongitude());
                intent.putExtras(extras);
                startActivity(intent);
                finish();
            }
        });

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);
        createGoogleApi();
    }

    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
    }



    private void addMarker(GoogleMap map, double lat, double lon,
                           int title, int snippet) {
        //  BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.pin);
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))

                .title(getString(title)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .snippet(getString(snippet))
                .draggable(true));

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick("+latLng +")");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
    }
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }

    private void startLocationUpdates(){
        Log.i(TAG, "st" +
                "artLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }




    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        writeActualLocation(location);
location.getLatitude();
        arrayList.add(new LatLng(location.getLatitude(),location.getLongitude()));
        int i;
        for(i=0;i<arrayList.size();i++)
        {
            mMap.addPolyline(new PolylineOptions().addAll(arrayList).width(10).color(Color.RED));

        }


    }

    private void writeActualLocation(Location location) {
        floc = location;
        textLat.setText( "Lat: " + location.getLatitude() );
        textLong.setText( "Long: " + location.getLongitude() );
        //sendDataToServer(location);
        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        arrayList.add(new LatLng(location.getLatitude(),location.getLongitude()));

    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1000);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case 1000: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
    }


    protected void onStart() {
        super.onStart();
        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( mMap!=null ) {
            // Remove the anterior marker
            if ( locationMarker != null )
                locationMarker.remove();
            locationMarker = mMap.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mMap.animateCamera(cameraUpdate);
        }
    }

    private String formatDataAsJSON(Location location) {
        final JSONObject root = new JSONObject();
        context = getApplicationContext();
        date = DateFormat.getDateTimeInstance().format(new Date());
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a");
        time = simpleDateFormat.format(calendar.getTime());
        latitude = location.getLatitude();
        longitude = location.getLatitude();
        username1 = 2;
        username2 = 3;
        try {
            JSONObject location2 = new JSONObject();
            location2.put("lat", latitude);
            location2.put("long", longitude);

            JSONObject group = new JSONObject();
            group.put("username1", username1.toString());
            group.put("username2", username2.toString());

            JSONObject username = new JSONObject();
            username.put("date", date);
            username.put("time", time);
            username.put("location", location2);
            username.put("group", group);

            root.put("username", username);
            return root.toString(1);
        } catch (JSONException e) {
            Log.d("JWP", "Can't format JSON");
        }
        return null;
    }

    private void sendDataToServer(Location location) {
        final String json = formatDataAsJSON(location);
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
        }catch (ClientProtocolException e) {
            Log.d("JWP", e.toString());
        }catch (IOException e) {
            Log.d("JWP", e.toString());
        }
        return "Unable to contact Server";
    }

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

