package com.dominoxpgaming.android.MapContent;


import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jan on 19.10.2016.
 *
 */

public class MapInteraction {
    private static final String tag ="MapInteraction";
    //TODO: move to settings
    private static final int MAX_COLLECTED_OBJECTS_CACHED = 20; //how many collected objects are beeing cached in 'collectedBuildings'/'collectedAreas' for better performence

    private static double radius = 1;

    private HashMap<Long,BuildingMarker> currentShownMarkers;
    private HashMap<Long,AreaPolygone> currentShownPolygones;

    private ArrayList<Long> collectedBuildings = new ArrayList<Long>();
    private ArrayList<Long> collectedAreas = new ArrayList<Long>();

    private double latitude = 0d;
    private double longitude = 0d;

    public MapInteraction(HashMap<Long,BuildingMarker> currentShownMarkers, HashMap<Long,AreaPolygone> currentShownPolygones, double radius){
        this.currentShownMarkers=currentShownMarkers;
        this.currentShownPolygones=currentShownPolygones;
        this.radius = radius/100000;
    }

    public void updateLocation(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;

        for (Map.Entry entry: currentShownMarkers.entrySet()){
            BuildingMarker building = (BuildingMarker) entry.getValue();
            GeoPoint geoPoint = building.getPosition();
            long key = (long) entry.getKey();

            if(collectedBuildings.contains(key)){
                continue;
            }

            if (isPointInBox(geoPoint)){
                if (isPointInCirlce(geoPoint)){


                    collectedBuildings.add(key);
                    collectBuilding(key,building.getBuildingId());
                }
            }
        }
    }

    public void updateLocation(GeoPoint geoPoint){
        this.updateLocation(geoPoint.getLatitude(),geoPoint.getLongitude());
    }

    public void collectBuilding(long id,int buildingID){
        Log.i(tag,"Collected Building ID:"+id+" type:"+buildingID);
    }

    private Boolean isPointInBox(GeoPoint geoPoint){
        //Check if GeoPoint is in range of a box radius (-r<x<r)
        if ((latitude-radius < geoPoint.getLatitude()) && (geoPoint.getLatitude() < latitude+radius)){
            if ((longitude-radius < geoPoint.getLongitude()) && (geoPoint.getLongitude() < longitude+radius)){
                return true;
            }
        }
        return false;
    }

    private Boolean isPointInCirlce(GeoPoint geoPoint){
        //Check if GeoPoint is in range of radius
        //http://stackoverflow.com/questions/481144/equation-for-testing-if-a-point-is-inside-a-circle

        double deltaLat = Math.abs(latitude - geoPoint.getLatitude());
        double deltaLon = Math.abs(longitude - geoPoint.getLongitude());

        return ((Math.pow(deltaLat,2d) + Math.pow(deltaLon,2d)) <= Math.pow(radius,2d));
    }

}
