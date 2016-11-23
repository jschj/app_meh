package com.dominoxpgaming.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.dominoxpgaming.android.MapContent.MapContent;
import com.dominoxpgaming.android.MapContent.MapLocationProvider;
import com.dominoxpgaming.android.mutilitys.ConvertUnits;
import com.dominoxpgaming.android.mutilitys.PermissionCheck;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainMapView extends AppCompatActivity {
    private final String tag = "MainMapView";

    private MapView map;
    private IMapController mapController;
    private CompassOverlay mCompassOverlay;

    private Boolean followLocation = true;
    private Boolean rotateWithCompass = false;
    private Float lastOrientation = 0f;

    private MapLocationProvider mapLocationProvider;
    private MapContent mapContent;
    private ServerApiV1 serverApi = new ServerApiV1();
    private LocalDBHandler databaseHandler = new LocalDBHandler(this);
    public ImageHandler imageHandler;

    private SharedPreferences sharedPref;


    @Override
    public void  onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case PermissionCheck.PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i(PermissionCheck.TAG,"Permission to 'Storage' was granted!");
                    // permission was granted, yay!
                    //just do nothing
                    recreate();

                }else{
                    Log.w(PermissionCheck.TAG,"Permission to 'Storage' was denied!, using internal SdCard Storage!");
                    // permission denied, boo!

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.external_storage_allowed), 0);
                    editor.apply();

                    // set Osmdroid Path to internal sdcard/ appdata path!
                    setOsmdroidPath();
                    //recreate();
                }
                //Check if we have Location Permission afterwards, because only one request at a time is permitted!
                PermissionCheck.checkLocationPermission(this);
                return;
            }

            case PermissionCheck.PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i(PermissionCheck.TAG,"Permission to 'Location' was granted");
                    // permission was granted, yay!
                }else{
                    Log.w(PermissionCheck.TAG,"Permission to 'Location' was denied!");
                    // permission denied, boo!
                }
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the content view to main map view
        setContentView(R.layout.activity_main_map_view);

        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();

        imageHandler = new ImageHandler(databaseHandler,this);


        Log.i(tag, "calling onCreate Methode, loading stuff...");
        sharedPref = this.getSharedPreferences(getString(R.string.sharedPrefMainMapView), Context.MODE_PRIVATE);
        //int useExternalStorage = sharedPref.getInt(getString(R.string.external_storage_allowed), 0);





        if (1 == sharedPref.getInt(getString(R.string.external_storage_allowed), 1)){
            Log.i(tag, "check if we have the sdcard permission");
            //we should check if we have the permission
            if (!PermissionCheck.checkSdCardPermission(this)){
                // set Osmdroid Path to internal sdcard/ appdata path!
                setOsmdroidPath();
            }
        }else{
            Log.i(tag, "check if we have the sdcard permission deactivated");
            // set Osmdroid Path to internal sdcard/ appdata path!
            setOsmdroidPath();
            //we dont have the permission but need to request Location anyways (handeld normaly after sdcard check)
            PermissionCheck.checkLocationPermission(this);
        }


        setExternalConstants();


        Double latitude = (double) sharedPref.getFloat(getString(R.string.lastknownlat),0f);
        Double longitude = (double) sharedPref.getFloat(getString(R.string.lastknownlon),0f);
        GeoPoint startPoint = new GeoPoint(latitude,longitude);

        setUpMap(startPoint);

        mapContent = new MapContent(map,this,serverApi,databaseHandler,imageHandler);

        //TODO:reenable
        //setUpGPSOverlay();


        mapContent.updateLastKnownLocation(latitude,longitude);
        setUpLocationDialogs(latitude,longitude);

        mapContent = new MapContent(map,this,serverApi,databaseHandler,imageHandler);

        //setUpGPSOverlay();

        setUpOrientationOverlay();

        setUpLocationFollow();

        Log.i(tag,OpenStreetMapTileProviderConstants.TILE_PATH_BASE.toString());
    }

    @Override
    public void onStart(){
        super .onStart();

        //do stuff
        //Start Map Update Thread
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //checkDataBase();
                mapContent.threadUpdateMapResources();
            }
        };
        Thread thread = new Thread(r);


        thread.setName("Thread - update map resouces");
        thread.start();
        mapLocationProvider.enableLocationProvider();
        mapLocationProvider.checkPoroviderStatus();

    }


    @Override
    public void onStop(){
        super .onStop();
        //do stuff
        //unblock function if blocked
        //Handler mainHandler = new Handler(getMainLooper());

        mapLocationProvider.disableLocationProvider();
        mapContent.suspendMapStuff();


        //Free Ram from Icons
        imageHandler.flushCachedIcons();

        Log.d(tag,databaseHandler.activeDatabaseCount+" DataBase connections are still open!");



        //Save all shared preferences
        SharedPreferences.Editor editor = sharedPref.edit();

        if (mapLocationProvider.lastlatitude != 0d && mapLocationProvider.lastlongitude != 0d){
            //Save location only if it is non (0,0)
            editor.putFloat(getString(R.string.lastknownlat), mapLocationProvider.lastlatitude.floatValue());
            editor.putFloat(getString(R.string.lastknownlon), mapLocationProvider.lastlongitude.floatValue());
        }
        editor.apply();

    }

    @Override
    public void onTrimMemory(int level){
        super .onTrimMemory(level);
        Log.w("Memory","We got a memory warning level:"+ Integer.toString(level));

    }


    private void checkDataBase(){
        HashMap<String,Object> result = serverApi.getImages(0);

        if (result == null || result.get("images") == null){
            Log.e(tag,"Image Result is null!");
        }


        ArrayList<HashMap<String, Object>> images = (ArrayList<HashMap<String, Object>>) result.get("images");

        for(HashMap<String, Object> entry:images){
            byte[] image = (byte[]) entry.get("image");
            long id = (long) entry.get("imageID");

            databaseHandler.insertImage(image,id);

        }





    }
    private void setOsmdroidPath(){
        File localOsmdroidPath = new File(getApplicationInfo().dataDir,"osmdroid");
        File localTilePath = new File(localOsmdroidPath,"tiles");

        //Update the constants
        OpenStreetMapTileProviderConstants.TILE_PATH_BASE = localTilePath;
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setCachePath(localTilePath.toString());
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setOfflineMapsPath(localOsmdroidPath.toString());
    }
    private void setExternalConstants(){
        //Set some external constants
        setOsmdroidPath();

        //set the orientation to portrait as the scanner only allowes this direction
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Keeps the Window on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Fix the UserAgent to Access MAPNIK Provider
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);

        setOsmdroidPath();
    }
    private void setUpMap(GeoPoint startPoint){
        //Bind the existing Mapview to object
        map = (MapView) findViewById(R.id.map);
        //Set the Tileset to MAPNIK, default openstreetmap provider
        map.setTileSource(TileSourceFactory.MAPNIK);


        //set the map to use online/offline maps,
        //[TODO] later editable in settings...
        map.setUseDataConnection(true);

        //get the map controller class
        mapController = map.getController();

        //enabling zooming for the user
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        //set the zomm factor range
        //TODO: Add this in the settings (advanced)
        map.setMaxZoomLevel(18);
        map.setMinZoomLevel(3);
        //for better zoom options (enables higher zoom)
        map.setTilesScaledToDpi(true);


        //set the current zoom and location
        mapController.setZoom(16);
        //TODO: Set this value to the last known position

        mapController.setCenter(startPoint);
        map.setMapOrientation(0f);


    }
    private void setUpLocationDialogs(double latitude,double longitude){
        //Setup Dialog for Missing Location Provider
        final Dialog missingLocationProviderDialog = new Dialog(this);
        missingLocationProviderDialog.setContentView(R.layout.fragment_no_location_provider);
        missingLocationProviderDialog.setTitle(getString(R.string.missing_location_provider));
        missingLocationProviderDialog.setCancelable(false);


        Button goToSettings = (Button) missingLocationProviderDialog.findViewById(R.id.go_to_settings);
        goToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                mapLocationProvider.checkPoroviderStatus();
            }
        });

        //TODO: Create Mainmenu/link to Mainmenu
        Button goToMainMenu = (Button) missingLocationProviderDialog.findViewById(R.id.go_to_mainmenu);
        goToMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                missingLocationProviderDialog.dismiss();
            }
        });

        //TODO: Create InGame/link to InGame
        Button goToIngame = (Button) missingLocationProviderDialog.findViewById(R.id.go_to_ingame);
        goToIngame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                missingLocationProviderDialog.dismiss();
            }
        });

        //Setup Dialog for Missing Location Permission
        final Dialog missingLocationPermission = new Dialog(this);
        missingLocationPermission.setContentView(R.layout.fragment_no_location_permission);
        missingLocationPermission.setTitle(getString(R.string.missing_location_permission));
        missingLocationPermission.setCancelable(false);

        final Activity activity = this;

        final Button grantLocationPermission = (Button) missingLocationPermission.findViewById(R.id.grant_location_permission);
        grantLocationPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionCheck.checkLocationPermission(activity)){
                    missingLocationPermission.dismiss();
                }
                else{
                    mapLocationProvider.enableLocationProvider();
                    if (mapLocationProvider.isLocationProviderEnabled()){
                        missingLocationPermission.dismiss();
                    }
                }
            }
        });

        final Button goToAppSettings = (Button) missingLocationPermission.findViewById(R.id.go_to_app_settings);
        goToAppSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
            }
        });

        mapLocationProvider = new MapLocationProvider(this,latitude,longitude){
            @Override
            public void onLocationChange(Location location) {
                mapContent.updateLocation(location.getLatitude(),location.getLongitude());
                if (followLocation){
                    mapController.animateTo( new GeoPoint(location));
                }
            }

            @Override
            public void onMissingEnabledLocationProvider() {
                missingLocationProviderDialog.show();
            }

            @Override
            public void onReEnabledLocationProvider() {
                missingLocationProviderDialog.dismiss();
            }

            @Override
            public void onMissingLocationPermission() { missingLocationPermission.show(); }
        };
    }
    private void setUpLocationFollow(){

        final ImageButton switchFollowLocation = (ImageButton) findViewById(R.id.switch_follow_location);


        switchFollowLocation.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (followLocation) {
                    Log.d("FollowLocation", "disabled follow location");
                    switchFollowLocation.setImageResource(R.drawable.icon_lastknown_position);
                    followLocation=false;
                } else {
                    Log.d("FollowLocation", "enable follow location");
                    mapController.animateTo(new GeoPoint(mapLocationProvider.lastlatitude,mapLocationProvider.lastlongitude));
                    switchFollowLocation.setImageResource(R.drawable.icon_current_position);
                    followLocation=true;
                }
            }
        });
    }
    private void setUpOrientationOverlay(){
        //Compass Orientation Overlay --------------------------------------------------------------
        Log.d(tag, "setup Orientation Provider");
        this.mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), map) {
            @Override
            public void onOrientationChanged(float orientation, IOrientationProvider source) {
                if (rotateWithCompass) {
                    if (!(((lastOrientation - 2) <= orientation) && ((lastOrientation + 2) >= orientation))) {
                        map.setMapOrientation(-(int) orientation);
                        lastOrientation = orientation;
                    }
                    mapContent.locationMarker.setRotation(0);
                }
                else{
                    mapContent.locationMarker.setRotation(orientation-360);
                }
                super.onOrientationChanged(orientation, source);
            }
        };


        final Button switch_compass = (Button) findViewById(R.id.switch_compass);
        switch_compass.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                if (rotateWithCompass) {
                    Log.d("CompassOL", "disable rotation with compass");
                    rotateWithCompass = false;
                    map.setMapOrientation(0f);
                } else {
                    Log.d("CompassOL", "enable rotation with compass");
                    rotateWithCompass = true;
                }
                return false;
            }
        });


        //Enable Overlay functions
        this.mCompassOverlay.enableCompass();

        //Set Compass location
        /*this.mCompassOverlay.setCompassCenter(
                ConvertUnits.convertDpToPixel(17f, this),
                ConvertUnits.convertDpToPixel(17f, this)
        );
        */


        this.mCompassOverlay.setCompassCenter(
                26,26
        );

        //Add Overlay to display
        map.getOverlays().add(this.mCompassOverlay);
    }





    private void testDB(){
        LocalDBHandler mdatabase = new LocalDBHandler(this);
    }
}