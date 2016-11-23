package com.dominoxpgaming.android.MapContent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dominoxpgaming.android.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

/**
 * Created by Jan on 17.10.2016.
 */

public class LocationMarker {
    private String tag = "LocationMarker";

    private final static String LAST_KNOWN_POSITION = "lastknownposition";
    private final static String CURRENT_POSITION = "currentposition";

    private String currentShownMarker = LAST_KNOWN_POSITION;


    private Marker marker;
    private Circle circle;
    private Drawable currentPosition;
    private Drawable lastKnownPosition;

    private MapView mapView;
    public GeoPoint geoPoint = new GeoPoint(0d,0d);

    public LocationMarker(MapView mapView, Context context, double radius){
        this.mapView = mapView;
        currentPosition = ContextCompat.getDrawable(context, R.drawable.direction_arrow);
        lastKnownPosition = ContextCompat.getDrawable(context, R.drawable.icon_lastknown_position);

        marker = new Marker(mapView);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER);
        marker.setIcon(lastKnownPosition);


        circle = new Circle(marker.getPosition(),radius,10);
        circle.setFillColor(0x15FF0080);
        circle.setStrokeColor(0x800000FF);
        circle.setStrokeWidth(5.0f);


        Overlay overlay = new Overlay() {
            @Override
            protected void draw(Canvas canvas, MapView mapView, boolean b) {
                circle.draw(canvas,mapView,b);
                marker.draw(canvas,mapView,b);
                }
            };


        mapView.getOverlays().add(overlay);
        mapView.invalidate();
    }

    public void setRotation(float rotation){
        if (currentShownMarker == LAST_KNOWN_POSITION){
            marker.setRotation(0);
        }
        else{
            marker.setRotation(rotation);
        }
    }

    public void setLastKnownLocation(double latitude,double longitude){
        updateLocation(latitude,longitude);
        if (currentShownMarker != LAST_KNOWN_POSITION){
            marker.setIcon(lastKnownPosition);
            mapView.invalidate();
            currentShownMarker=LAST_KNOWN_POSITION;
        }
        Log.e(tag,"lastknown");
    }

    public void setCurrentPosition(double latitude,double longitude){
        updateLocation(latitude,longitude);

        if (currentShownMarker != CURRENT_POSITION){
            marker.setIcon(currentPosition);
            mapView.invalidate();
            currentShownMarker=CURRENT_POSITION;
        }
        Log.e(tag,"current");
    }

    private void updateLocation(double latutude, double longitude){
        geoPoint.setLatitude(latutude);
        geoPoint.setLongitude(longitude);

        marker.setPosition(geoPoint);
        mapView.invalidate();
        circle.updateMiddlePoint(geoPoint);
        mapView.invalidate();

    }
}
