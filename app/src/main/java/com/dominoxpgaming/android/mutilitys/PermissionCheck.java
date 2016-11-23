package com.dominoxpgaming.android.mutilitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.os.Build.VERSION.SDK_INT;

/**
 *
 * Created by Jan on 22.09.2016.
 */

public class PermissionCheck {

    public final static String TAG = "PermissionCheck";
    private final static Boolean ignoreSDKVersion = (SDK_INT < 23);

    public final static int PERMISSIONS_REQUEST_DEFAULT = 0;
    public final static int PERMISSIONS_REQUEST_LOCATION = 1;
    public final static int PERMISSIONS_REQUEST_STORAGE = 2;

    @SuppressLint("NewApi")
    public static Boolean checkLocationPermission(Activity activity){
        if (ignoreSDKVersion){Log.d(TAG,"Current Android SDK is not needed to request permission at runtime!");return true;}

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            Log.i(TAG,"We dont have the 'Location' Permission, requesting it!");
            String[] strings = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.LOCATION_HARDWARE};

            //Ignore SDK minVersion Alerts, SDKVersion is already catched above...
            activity.requestPermissions(strings,PERMISSIONS_REQUEST_LOCATION);
            return false;
        }else{
            Log.d(TAG,"We have the 'Location' Permission, yay!");
        }


        return true;
    }

    @SuppressLint("NewApi")
    public static Boolean checkSdCardPermission(Activity activity){
        if (ignoreSDKVersion){Log.d(TAG,"Current Android SDK is not needed to request permission at runtime!");return true;}

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            Log.i(TAG,"We dont have the 'Storage' Permission, requesting it!");
            String[] strings = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

            //Ignore SDK minVersion Alerts, SDKVersion is already catched above...
            activity.requestPermissions(strings,PERMISSIONS_REQUEST_STORAGE);
            return false;
        }else{
            Log.d(TAG,"We have the 'Storage' Permission, yay!");
        }
        return true;
    }


}

/*
ContextCompat.checkSelfPermission(this, Manifest.permission.LOCATION_HARDWARE)


private Boolean checkPermissions(){
        // Assume thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.LOCATION_HARDWARE) == PackageManager.PERMISSION_GRANTED){
            Log.d("PermissionCk","");
        }
        return false;
    }
 */