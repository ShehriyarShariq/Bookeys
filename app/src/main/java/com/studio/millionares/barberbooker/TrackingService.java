package com.studio.millionares.barberbooker;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class TrackingService extends Service {

    /*
        GPS TRACKING SERVICE FOR CURRENT LOCATION
    */

    public TrackingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        requestLocationUpdates();
    }

    private void requestLocationUpdates(){
        LocationRequest locationRequest = new LocationRequest();

        // Instant update
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permission == PackageManager.PERMISSION_GRANTED){
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location lastLocation = task.getResult();
                        Intent currentLocationIntent = new Intent("locationInfo");
                        currentLocationIntent.putExtra("latitude", String.valueOf(lastLocation.getLatitude()));
                        currentLocationIntent.putExtra("longitude", String.valueOf(lastLocation.getLongitude()));

                        LocalBroadcastManager.getInstance(TrackingService.this).sendBroadcast(currentLocationIntent);
                    }
                }
            });
        }
    }
}
