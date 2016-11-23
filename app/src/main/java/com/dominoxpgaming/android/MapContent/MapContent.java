package com.dominoxpgaming.android.MapContent;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;

import com.dominoxpgaming.android.ImageHandler;
import com.dominoxpgaming.android.LocalDBHandler;
import com.dominoxpgaming.android.ServerApiV1;
import com.dominoxpgaming.android.mutilitys.ConvertUnits;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jan on 17.10.2016.
 *
 */

public class MapContent {
    //LogTag of this Class used to identify origin
    private static final String tag = "MapContent";

    //Variables used in the Constructor for initialising
    private MapView map;
    private Context context;
    private MapInteraction mapInteraction;
    public LocationMarker locationMarker;

    private ServerApiV1 serverApi;
    private LocalDBHandler databaseHandler;
    private ImageHandler imageHandler;


    private final  static double radius = 20;

    //Gridstates
    private final int GRID_STATE_DONE = 1;
    private final int GRID_STATE_UNPROCESSED = 2;
    private final int GRID_STATE_PROCESSING = 3;
    private final int GRID_STATE_ERROR = -1;

    //count of Maximum Threads avaible
    private static int maxRequestThreads = 1;

    private Integer gridsensiblity = 2;
    private Float gridsize = 1000f;
    private final HashMap<ArrayList<Double>,Integer> gridqueue = new HashMap<>();
    
    private Boolean currentRunning = true;
    private final Boolean onLocationChange = false;

    private Double latitudegrid = 0d;
    private Double longitudegrid = 0d;

    private int currentRequestThreads = 0;

    private HashMap<Long,BuildingMarker> currentShownMarkers = new HashMap<>();
    private HashMap<Long,AreaPolygone> currentShownPolygones = new HashMap<>();


    public MapContent(MapView map,Context context,ServerApiV1 serverApi,LocalDBHandler databaseHandler, ImageHandler imageHandler){
        this.map = map;
        this.context = context;
        this.serverApi = serverApi;
        this.databaseHandler = databaseHandler;
        this.imageHandler = imageHandler;
        this.locationMarker = new LocationMarker(map,context,radius);
        this.mapInteraction = new MapInteraction(currentShownMarkers,currentShownPolygones,radius);

        Overlay overlay = new Overlay() {
            @Override
            protected void draw(Canvas canvas, MapView mapView, boolean b) {
                for (Map.Entry entry:currentShownMarkers.entrySet()){
                    Marker marker = (Marker) entry.getValue();
                    marker.draw(canvas,mapView,b);
                }
            }
        };

        map.getOverlays().add(0,overlay);
        map.invalidate();



    }


    //Getter and Setter
    public static int getMaxRequestThreads() {
        return maxRequestThreads;
    }
    public static void setMaxRequestThreads(int count){
        //Make sure we have at least on request Thread to proccess Requests!
        if (count > 1) {
            maxRequestThreads = count;
        }else{
            maxRequestThreads = 1;
        }
    }
    public void updateLocation(Double latitude,Double longitude){
        latitudegrid = ConvertUnits.round(longitude, gridsensiblity);
        longitudegrid = ConvertUnits.round(latitude, gridsensiblity);

        synchronized (onLocationChange){
            onLocationChange.notify();
        }

        locationMarker.setCurrentPosition(latitude,longitude);

        mapInteraction.updateLocation(latitude,longitude);
    }

    public void updateLastKnownLocation(Double latitude,Double longitude){
        latitudegrid = ConvertUnits.round(longitude, gridsensiblity);
        longitudegrid = ConvertUnits.round(latitude, gridsensiblity);

        synchronized (onLocationChange){
            onLocationChange.notify();
        }

        locationMarker.setLastKnownLocation(latitude,longitude);
    }

    public void enableThreads(){
        currentRunning=true;
    }
    public void disableThreads(){
        currentRunning=false;
    }

    //other Methods

    public void threadUpdateMapResources(){
        Log.i(tag,"Request thread here!");
        enableThreads();

        ArrayList<ArrayList<Double>> requestqueue = new ArrayList<>();

        //Grid order, this is the order witch the chunks get loaded / requested (0,0) is middle
        int gridorder[][] = {{0, 0},
                {0, 1},
                {1, 0},
                {-1, 0},
                {0, -1},
                {-1, -1},
                {1, 1},
                {-1, 1},
                {1, -1}};

        while (currentRunning) {
            //loop for continuing loading of new maptiles
            double startlat = this.latitudegrid;
            double startlon = this.longitudegrid;

            requestqueue.clear();

            //calculate requierd grids
            for (int[] grid: gridorder){
                double gridlat = startlat+grid[0]*(gridsize/100000);
                double gridlon = startlon+grid[1]*(gridsize/100000);

                ArrayList<Double> request = new ArrayList<>();
                request.add(gridlat);
                request.add(gridlon);

                requestqueue.add(request);

                //Add grid to queue if not exists
                if(!(gridqueue.containsKey(request))){
                    gridqueue.put(request,GRID_STATE_UNPROCESSED);
                }
            }


            Log.d(tag,"Requestgrid:"+gridqueue.toString());

            HashMap<ArrayList<Double>,Integer> shadowCopyQueue;

            //create shadow copy for removing items out of a for-loop
            shadowCopyQueue = (HashMap<ArrayList<Double>,Integer>) gridqueue.clone();

            //remove all old query entrys if no longer in grid
            for (Map.Entry < ArrayList<Double>,Integer > entry:shadowCopyQueue.entrySet()){
                ArrayList<Double> coords = entry.getKey();

                if(!requestqueue.contains(coords)){
                    switch (entry.getValue()){
                        case(GRID_STATE_PROCESSING):{
                            gridqueue.put(entry.getKey(),GRID_STATE_UNPROCESSED);
                            break;
                        }
                        case(GRID_STATE_DONE):{
                            gridqueue.remove(entry.getKey());
                            break;
                        }
                        case(GRID_STATE_ERROR):{
                            gridqueue.put(entry.getKey(),GRID_STATE_UNPROCESSED);
                            break;
                        }
                        case(GRID_STATE_UNPROCESSED):{
                            gridqueue.remove(entry.getKey());
                            break;
                        }
                    }
                }
            }

            //Mem freeing?
            shadowCopyQueue.clear();



            //Process gridqueue
            for (Map.Entry < ArrayList<Double>,Integer > entry:gridqueue.entrySet()){
                final ArrayList<Double> grid = entry.getKey();
                double gridlat = grid.get(0);
                double gridlon = grid.get(1);
                int requeststate = entry.getValue();

                switch (requeststate){
                    case (GRID_STATE_PROCESSING):{
                        //Request is already processing, ignoring it and let it work...
                        break;
                    }
                    case (GRID_STATE_DONE):{
                        //Grid has already been processed, ignore it
                        break;
                    }
                    case (GRID_STATE_UNPROCESSED):{
                        //Grid is unprocessed, processing it!

                        while (startlat == latitudegrid && startlon == longitudegrid){//check if we are still standing still
                            //is there a thread not used yet/free
                            if(currentRequestThreads < maxRequestThreads){
                                Log.d(tag,"Starting new Thread because maxThreads is not reached!");

                                //Set the grid into process state
                                gridqueue.put(grid,GRID_STATE_PROCESSING);

                                Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        updateMapTile(grid);
                                    }
                                };

                                Thread thread = new Thread(r);
                                thread.setName("mapResource_No"+currentRequestThreads+"/"+maxRequestThreads+"_"+gridlat+":"+gridlon);
                                thread.start();

                                //Update Threadcount
                                currentRequestThreads++;
                                break;
                            }
                            else{
                                Log.d(tag,"waiting for Thread because maxThreads was reached!");
                                synchronized (gridqueue){
                                    try{
                                        // Calling wait() will block this thread until another thread
                                        // calls notify() on the object.
                                        gridqueue.wait();
                                    }catch (InterruptedException e){
                                        //Happens if someone interrupts your thread.
                                    }
                                }
                                //a old queue entry should have been processed
                                //we haven't moved, check if a thread is avaible
                            }
                        }
                        break;
                    }
                    case (GRID_STATE_ERROR):{
                        //show Errormessage and ignore this Grid
                        gridqueue.put(grid,GRID_STATE_DONE);
                        break;
                    }
                }

                //Check if we are standing still of moved out fo the current chunk
                if (!(startlat == latitudegrid && startlon == longitudegrid)) {
                    Log.d(tag,"We moved out of grid, recalculating...");
                    break;
                }
            }


            while(currentRequestThreads!=0){
                synchronized (gridqueue){
                    try{
                        // Calling wait() will block this thread until another thread
                        // calls notify() on the object.
                        gridqueue.wait();
                    }catch (InterruptedException e){
                        //Happens if someone interrupts your thread.
                    }
                }
            }

            //the whole gridqueue was processed of we moved along
            removeOldMakers(latitudegrid-(gridsize/100000),longitudegrid-(gridsize/100000),(gridsize/100000)*3,(gridsize/100000)*3);


            //Check if we are standing still of moved out fo the current chunk
            if (!(startlat == latitudegrid && startlon == longitudegrid)) {
                //instant update
                Log.d(tag,"We moved out of grid, recalculating...");
                continue;
            }
            else {
                Log.d(tag,"Waiting for event");
                //wait until a gridchange has made
                synchronized (onLocationChange) {
                    try {
                        // Calling wait() will block this thread until another thread
                        // calls notify() on the object.
                        onLocationChange.wait();
                    } catch (InterruptedException e) {
                        //Happens if someone interrupts your thread.
                    }
                }
                Log.d(tag,"continuing");
                //a old queue should have finished
                //we haven't moved, check if a thread is avaible
            }

            Log.d(tag,currentRequestThreads+"/"+maxRequestThreads+" threads currently activ");
        }

        Log.i(tag,"Map Resource Updater is suspending now!");
    }

    private void updateIconsOnMap(HashMap result){

        //Update Nodes on Map
        @SuppressWarnings("unchecked")
        ArrayList<HashMap> nodes = (ArrayList) result.get("nodes");



        for (@SuppressWarnings("unchecked")
                HashMap < String, Object > hashMap: nodes){
            double lat = (double) hashMap.get("lat");
            double lon = (double) hashMap.get("lon");
            long id = (long) hashMap.get("nodeID");

            if (currentShownMarkers.containsKey(id)){
                //Marker is already in list, ignoring it
                continue;
            }

            //build Marker
            BuildingMarker building = new BuildingMarker(map);
            GeoPoint coordinates = new GeoPoint(lon,lat);
            building.setPosition(coordinates);
            building.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
            building.setBuildingId( (int) hashMap.get("buildingID"));


            //map.getOverlays().add(building);
            //map.invalidate();

            int imageID = (int)(long) hashMap.get("imageID");


            building.setIcon(
                    imageHandler.getCachedIcons(
                            imageID,
                            (int) ConvertUnits.convertDpToPixel(40f, context),
                            (int) ConvertUnits.convertDpToPixel(40f, context))
            );

            building.setTitle("PointID: "+id);

            currentShownMarkers.put(id,building);

            map.invalidate();
        }



        //Update Polygones on Map
        @SuppressWarnings("unchecked")
        HashMap<Long,HashMap<String,Object>> polygones = (HashMap<Long,HashMap<String,Object>>) result.get("polygones");
        for (HashMap.Entry < Long, HashMap<String,Object>> hashMap: polygones.entrySet()){
            //PolygonID
            //long polyId = hashMap.getKey();

            //Polygon Attributes
            HashMap<String,Object> polygonAttributes = hashMap.getValue();
            @SuppressWarnings("unchecked")
            ArrayList<HashMap<String,Object>> coords = (ArrayList<HashMap<String,Object>>) polygonAttributes.get("coords");

            long id = (long) polygonAttributes.get("polygonID");

            if (currentShownPolygones.containsKey(id)) {
                //Polygon is already in list, ignoring it
                continue;
            }


            ArrayList<GeoPoint> polygonPoints = new ArrayList<>();

            //Polygon FrameWork
            AreaPolygone polygon = new AreaPolygone();

            for(HashMap <String,Object> coord: coords){
                double lat = (double) coord.get("lat");
                double lon = (double) coord.get("lon");
                int position = (int) coord.get("pos");
                //long coordId = (long) coord.get("coordID");

                polygonPoints.add(new GeoPoint(lon, lat));

            }

            polygon.setPoints(polygonPoints);

            polygon.setFillColor(0x15FF0080);
            polygon.setStrokeColor(0x800000FF);
            polygon.setStrokeWidth(5.0f);
            polygon.setTitle("TEST");

            currentShownPolygones.put(id,polygon);
            //currentShownMarkers.put(id,polygon);


            map.getOverlays().add(polygon);
            map.invalidate();
        }


        /*
        Polygon polygon = new Polygon();
        polygon.setPoints(list);

        //polygon.



        MotionEvent event = MotionEvent.obtain(1, 1, MotionEvent.ACTION_HOVER_ENTER, (float)23.21602, (float) 72.64926, 1);
        Log.e("CONTAINS", polygon.contains(event)+"");


        */
    }
    
    private void updateMapTile(ArrayList<Double> liste){

        double lat = liste.get(0);
        double lon = liste.get(1);

        final HashMap result;


        if (databaseHandler.isGridCached(lat,lon,gridsize/100000)){
            Log.d(tag,"Requested Grid is already cached, returning from DataBase!");
            result=databaseHandler.getGridCached(lat,lon,gridsize/100000);
            //Log.d(tag,result.toString());
        }else{
            Log.d(tag,"Requested Grid is not yet cached, getting it from Server!");

            @SuppressWarnings("unchecked")
            HashMap<String,Object> tempresult = (HashMap<String,Object>) serverApi.getMapTileInfo(liste.get(0),liste.get(1),gridsize/100000,gridsize/100000);
            if (tempresult == null){
                Log.e(tag,"Request failed!");

                currentRequestThreads--;
                gridqueue.put(liste,GRID_STATE_UNPROCESSED);//GRID_STATE_ERROR
                synchronized (gridqueue){
                    gridqueue.notify();
                }
                return;
            }
            else {
                //Insert downloaded Data into DataBase
                databaseHandler.insertGridCache(lat,lon,tempresult);
                result = databaseHandler.getGridCached(lat,lon,gridsize/100000);
            }
        }

        //update map on main thread
        Handler mainHandler = new Handler(context.getMainLooper());

        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                // run code
                updateIconsOnMap(result);
                map.invalidate();
            }
        });

        currentRequestThreads--;
        gridqueue.put(liste,GRID_STATE_DONE);
        synchronized (gridqueue){
            gridqueue.notify();
        }
    }

    private void removeOldMakers(double lat, double lon, float sizex, float sizey){

        ArrayList<Long> removeItems = new ArrayList<>();

        for (Map.Entry < Long, BuildingMarker> hashMap: currentShownMarkers.entrySet()){
            double lonn = hashMap.getValue().getPosition().getLatitude();
            double latt = hashMap.getValue().getPosition().getLongitude();

            if (!(((lat <= latt) && (latt <= lat+sizex)) && ((lon <= lonn) && (lonn <= lon+sizey)))){
                //Marker is not in view range, remove it
                Marker marker = hashMap.getValue();
                map.getOverlays().remove(marker);

                removeItems.add(hashMap.getKey());
            }
        }

        for(Long item: removeItems){
            currentShownMarkers.remove(item);
        }

        removeItems.clear();

        try {
            for (Map.Entry<Long, AreaPolygone> hashMap : currentShownPolygones.entrySet()) {
                long id = hashMap.getKey();
                Polygon polygon = hashMap.getValue();

                for (int i = 0; i <= polygon.getPoints().size() - 1; i++) {
                    GeoPoint point = polygon.getPoints().get(i);

                    double lonn = point.getLongitude();
                    double latt = point.getLatitude();

                    if (!(((lat <= latt) && (latt <= lat + sizex)) && ((lon <= lonn) && (lonn <= lon + sizey)))) {
                        //Polygon is not in view range, remove it
                        map.getOverlays().remove(polygon);

                        removeItems.add(id);
                    }
                }
            }
        }catch(java.util.ConcurrentModificationException e){
            Log.w(tag,"Could not remove old marker, because of "+e.toString());
        }

        for(Long item: removeItems){
            currentShownPolygones.remove(item);
        }

    }
    
    private void removeAllMarkers(){
        for (Map.Entry < Long, BuildingMarker> hashMap: currentShownMarkers.entrySet()){
            Marker marker = hashMap.getValue();
            map.getOverlays().remove(marker);
        }
        currentShownMarkers.clear();

        for (Map.Entry<Long, AreaPolygone> hashMap : currentShownPolygones.entrySet()) {
            map.getOverlays().remove(hashMap.getValue());
        }

        currentShownPolygones.clear();
    }

    public void suspendMapStuff(){
        //Clear Cache and stop all unecesarry stuff
        disableThreads();

        //notify all waiting threads
        synchronized (onLocationChange){
            onLocationChange.notify();
        }
        synchronized (gridqueue){
            gridqueue.notify();
        }

        removeAllMarkers();

    }

}
