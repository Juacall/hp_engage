package apps.hp.hp_engage;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by bernajua on 10/20/2015.
 */
public class AuraLocator  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final String TAG = "Aura Locator";
    private static AuraDetails[] auras;
    private GoogleApiClient   mGoogleApiClient;
    private Location mLastLocation,mCurrentLocation;
    private LocationRequest mLocationRequest;
    private Boolean mRequestingLocationUpdates = false,mSubscribedToLocationUpdates = false;
    private static MainActivity act;
    private static ArrayList marked = new ArrayList();
    private static double range = 0.00189394;// 10 feed in miles
    private static int markCount=0;


    public AuraLocator()
    {
        auras = null;
        act = null;
    }

    public AuraLocator(AuraDetails[] auras,MainActivity act)
    {
        this.auras = auras;
        this.act = act;
    }

    public AuraLocator(MainActivity act)
    {
        this.act = act;
    }


    public void setAuras(AuraDetails[] auras){
        this.auras = auras;
    }

    public void startLocationMonitoring()
    {
        try {
            if(!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(act)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.i(TAG, "Building Google Api Client");
        Toast.makeText(act, "Building Google Api Client", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle bundle) {

        getLastLocation();
        mRequestingLocationUpdates = true;
        Log.i(TAG, "Google Api Client Connected");

        Toast.makeText(act,"Google Api Client Connected", Toast.LENGTH_LONG).show();
        if (mRequestingLocationUpdates) {
            createLocationRequest();
            startLocationUpdates();
        }

    }

    private void getLastLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.i(TAG, "Getting Last Location");
        Toast.makeText(act,"Getting Last Location", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int i) {

        Toast.makeText(act,"Connection Suspended", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(act,"Connection Failed", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Connection Failed");
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Toast.makeText(act,"Creating Location Request", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Creating Location Request");
    }

    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Toast.makeText(act,"Starting Location Updates", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Starting Location Updates");

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mRequestingLocationUpdates = false;
        mSubscribedToLocationUpdates = true;
        //  mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //  updateUI();
        Toast.makeText(act,"Location Changed", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Location Changed");


        try{
            for(int i = 0; i < auras.length; i++) {

                if (distance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude() , auras[i].lat, auras[i].lon) < range) { // if distance < 0.1 miles we take locations as equal


                    //sendNotification("You are near a scannable aura!");
                    //break;
                }
            }


        }catch(NullPointerException e)
        {
            if(auras == null)
                return;

            getLastLocation();
            for(int i = 0; i < auras.length; i++) {

                if (distance(mLastLocation.getLatitude(),mLastLocation.getLongitude() , auras[i].lat, auras[i].lon) < range) { // if distance < 0.1 miles we take locations as equal
                   // sendNotification("You are near a scannable aura!");
                   // break;
                }
            }
        }



    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Toast.makeText(act,"Stopping Location Updates", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Stopping Location Updates");
    }

    /** calculates the distance between two locations in MILES */
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(act, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(act, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(act)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Aura Found")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
