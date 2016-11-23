package com.dominoxpgaming.android.MapContent;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.views.MapView;

import java.util.List;

/**
 * Created by Jan on 17.10.2016.
 *
 */

public class MapLocationProvider {
    private static final String tag = "MapLocationProvider";

    public Double lastlatitude = 0d;
    public Double lastlongitude = 0d;

    private Boolean providerGpsEnabled = true;
    private Boolean providerNetworkEnabled = true;
    private Boolean anyLocationProvider = true;

    private Boolean locationListenerEnabled = false;
    List<String> locationProviders;

    private LocationManager locationManager;
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.v(tag,"Location: " + location.toString());
            lastlatitude = location.getLatitude();
            lastlongitude = location.getLongitude();
            onLocationChange(location);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.v(tag,"StatusChanged: " + provider +" Status:"+Integer.toString(status) + "Extras:"+extras.toString());
        }

        @Override
        public void onProviderEnabled(String provider) {
            onProviderChange(provider,true);
        }

        @Override
        public void onProviderDisabled(String provider) {
            onProviderChange(provider,false);
        }
    };


    public MapLocationProvider(Context context,Double lastlatitude, Double lastlongitude){
        this.lastlatitude = lastlatitude;
        this.lastlongitude = lastlongitude;


        //Setup Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationProviders = locationManager.getAllProviders();

        //information about Location status
        Log.i(tag,"Location Provider Overview:");
        for(String s : locationManager.getAllProviders()){
            Log.d(tag,"Provider '"+s+"' is enabled: "+locationManager.isProviderEnabled(s));
        }

        enableLocationProvider();
    }


    public void enableLocationProvider(){
        if (!locationListenerEnabled){
            locationListenerEnabled=true;

            try {
                if(locationProviders.contains(LocationManager.NETWORK_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);
                }
                if(locationProviders.contains(LocationManager.GPS_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
                }

            }catch (SecurityException e){
                Log.i(tag,"User refused Location Permission!, we dont get Location updates!");
                onMissingLocationPermission();
                locationListenerEnabled=false;
            }

        }else{
            Log.w(tag,"LocationProvider was already enabled!");
        }

    }

    public void disableLocationProvider(){
        if (locationListenerEnabled) {
            locationListenerEnabled=false;
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }else{
            Log.w(tag,"LocationProvider was already disabled!");
        }
    }

    public Boolean isLocationProviderEnabled(){
        return locationListenerEnabled;
    }

    public void checkPoroviderStatus(){
        Boolean providerOnline = false;
        for (String provider: locationManager.getAllProviders()){
            if (locationManager.isProviderEnabled(provider)){
                providerOnline = true;
                break;
            }
        }


        //prevent multiple calls of methods
        if (!anyLocationProvider == providerOnline){
            if (providerOnline){
                onReEnabledLocationProvider();
            }else{
                onMissingEnabledLocationProvider();
            }
        }
        anyLocationProvider=providerOnline;
    }

    private void onProviderChange(String provider,Boolean state){
        if(state){//if Provider was enabled
            Log.v(tag,"Provider enabled:"+provider);
            switch (provider){
                case (LocationManager.GPS_PROVIDER):{
                    this.providerGpsEnabled = true;
                }
                case (LocationManager.NETWORK_PROVIDER):{
                    this.providerNetworkEnabled = true;
                }
            }
        }
        else{//if Provider was disabled
            Log.v(tag,"Provider disabled:"+provider);
            switch (provider){
                case (LocationManager.GPS_PROVIDER):{
                    this.providerGpsEnabled = false;
                }
                case (LocationManager.NETWORK_PROVIDER):{
                    this.providerNetworkEnabled = false;
                }
            }
        }


        //check if at least one Provider is enabled
        Boolean prevoiusLocationProvider = anyLocationProvider;
        anyLocationProvider = (providerGpsEnabled || providerNetworkEnabled);

        //prevent multiple calls of methods
        if (!anyLocationProvider == prevoiusLocationProvider){
            if (anyLocationProvider){
                onReEnabledLocationProvider();
            }else{
                onMissingEnabledLocationProvider();
            }
        }
    }


    public void onMissingEnabledLocationProvider(){
        Log.e(tag,"Please override Method onMissingEnabledLocationProvider() !");
    }

    public void onReEnabledLocationProvider(){
        //This Method gets called, if any LocationProvider was turned on again
        Log.e(tag,"Please override Method onReEnabledLocationProvider() !");
    }

    public void onMissingLocationPermission(){
        //This Method gets called, if Permission for localisation was forbidden
        Log.e(tag,"Please override Method onMissingLocationPermission() !");
    }


    public void onLocationChange(Location location){
        //Override this Methode to recieve Location Updates
    }
}
