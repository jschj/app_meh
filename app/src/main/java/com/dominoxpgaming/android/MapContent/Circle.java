package com.dominoxpgaming.android.MapContent;

import android.graphics.Canvas;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

/**
 * Created by Jan on 20.10.2016.
 */
public final class Circle extends Polygon {
    private GeoPoint middlePoint;
    private double radius;
    private int steps;

    //private double lat;
    //private double lon;


    public Circle(GeoPoint middlePoint, double radius, int steps) {
        this.middlePoint = new GeoPoint(middlePoint);
        this.radius = radius;
        this.steps = steps;


        //lat=this.middlePoint.getLatitude();
        //lon=this.middlePoint.getLongitude();

        //Create circle
        ArrayList<GeoPoint> circlePoints = new ArrayList<GeoPoint>();
        for (float f = 0; f < 360; f += steps) {
            circlePoints.add(middlePoint.destinationPoint(radius, f));
        }
        this.setPoints(circlePoints);


    }

    public void updateMiddlePoint(GeoPoint mPoint) {
        ArrayList<GeoPoint> circlePoints = new ArrayList<GeoPoint>();
        for (float f = 0; f < 360; f += steps) {
            circlePoints.add(mPoint.destinationPoint(radius, f));
        }
        this.setPoints(circlePoints);
    }

    @Deprecated
    public void updateMiddlePointRelative(GeoPoint mPoint) {
        /*
        if(false){// (middlePoint.getLatitude()== this.middlePoint.getLatitude() && middlePoint.getLongitude() == this.middlePoint.getLongitude()){
            return;
        }else{

            Log.i("DELTA",mPoint.getLatitude()+" "+lat);

            double latitudeDelta  = mPoint.getLatitude() - lat;
            double longitudeDelta = mPoint.getLongitude() - lon;

            Log.i("LATDELTA",Double.toString(latitudeDelta));
            Log.i("LONDELTA",Double.toString(longitudeDelta));

            List<GeoPoint> points = this.getPoints();

            int index = 0;
            for(GeoPoint point: points){
                point.setLatitude(point.getLatitude()+latitudeDelta);
                point.setLongitude(point.getLongitude()+longitudeDelta);
                points.set(index,point);
                index++;
            }

            this.setPoints(points);

            //this.setPoints(points);
        }

        middlePoint=mPoint;
        lat=mPoint.getLatitude();
        lon=mPoint.getLongitude();
        */
    }


    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
    }
}
