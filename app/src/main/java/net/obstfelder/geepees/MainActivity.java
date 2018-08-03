package net.obstfelder.geepees;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener
{
    private static String TAG = "GeePeeS";
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private float MAX_DISTANCE_BEFORE_UPDATE = 50f;
    private boolean inProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"PISS");
        if ( ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{  android.Manifest.permission.ACCESS_FINE_LOCATION  },1);
        }
        else
        {
            try {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();

                locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10000)
                        .setFastestInterval(10000);

                if (!googleApiClient.isConnected() || !googleApiClient.isConnecting() && !inProgress) {
                    inProgress = true;
                    googleApiClient.connect();
                }
            }
            catch(Exception e)
            {
                Log.e(TAG,"Exception during initialization: "+e.toString());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        try{
            float distInMeters = location.distanceTo(lastLocation);
            if(distInMeters>MAX_DISTANCE_BEFORE_UPDATE)
            {
                lastLocation = location;
            }
        }
        catch(Exception e)
        {
            Log.e(TAG,"Error in LocationChanged: "+e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (permissions.length == 1 && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
            else
            {
                try
                {
                    Toast.makeText(getBaseContext(), "Cannot activate this app without this permisson. Closing.", Toast.LENGTH_LONG).show();
                    Thread.sleep(3000);
                    finishAndRemoveTask();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    Log.e(TAG, "Fucked up thread handling");
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        try
        {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if(location==null)
            {
                return;
            }
            lastLocation = location;

            try
            {
                PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
            catch (IllegalStateException e) {}

            Log.i(TAG,"LOCATION\nAlt: "+location.getAltitude()+"\nBearing: "+location.getBearing()+"\nAcc: "+location.getAccuracy()+"\nLat: "+location.getLatitude()+"\nLong: "+location.getLongitude()+"\nSpeed: "+location.getSpeed());
        }
        catch(SecurityException sec)
        {

        }
        catch(Exception e)
        {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        inProgress = false;

        if (connectionResult.hasResolution())
        {
            try
            {
                connectionResult.startResolutionForResult(null,9000);
            }
            catch (IntentSender.SendIntentException e)
            {
                Log.e(TAG,"Error on intent and connection to Google API: "+e.toString());
            }
        }
        else
        {
            Log.e(TAG,"Error on connection to Google API: ");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting_menu, menu);
        return true;
    }
}
