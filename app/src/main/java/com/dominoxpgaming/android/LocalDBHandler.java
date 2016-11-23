package com.dominoxpgaming.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jan on 16.09.2016.
 */
public class LocalDBHandler extends SQLiteOpenHelper{

    private static final String tag = "LocalDB";

    private static final int DATABASE_VERSION = 1 ;
    private static final String DATABASE_NAME = "mapstuff.db";

    private static final String TABLE_RESOURCES_NAME = "resources";
    private static final String FIELD_RESOURCES_RESOURCEID = "resourceID";
    private static final String FIELD_RESOURCES_NAME = "name";
    private static final String FIELD_RESOURCES_IMAGEID = "imageID";

    private static final String TABLE_AREAS_NAME = "areas";
    private static final String FIELD_AREAS_AREAID = "areaID";
    private static final String FIELD_AREAS_NAME = "name";

    private static final String TABLE_IMAGES_NAME = "images";
    private static final String FIELD_IMAGES_IMAGEID = "imageID";
    private static final String FIELD_IMAGES_IMAGE = "image";

    private static final String TABLE_BUILDINGS_NAME = "buildings";
    private static final String FIELD_BUILDINGS_BUILDINGID = "buildingID";
    private static final String FIELD_BUILDINGS_NAME = "name";
    private static final String FIELD_BUILDING_IMAGEID = "imageID";

    private static final String TABLE_BUILDINGCONTAINSRESOURCE_NAME = "building_contains_resource";
    private static final String FIELD_BUILDINGCONTAINSRESOURCE_BUILDINGID = "buildingID";
    private static final String FIELD_BUILDINGCONTAINSRESOURCE_RESOURCEID = "resourceID";

    private static final String TABLE_AREACONTAINSRESOURCE_NAME = "area_contains_resoruce";
    private static final String FIELD_AREACONTAINSRESOURCE_AREAID = "areaID";
    private static final String FIELD_AREACONTAINSRESOURCE_RESOURCEID = "resourceID";

    private static final String TABLE_POLYGONES_NAME = "polygones";
    private static final String FIELD_POLYGONES_AREAID = "areaID";
    private static final String FIELD_POLYGONES_POLYGONID = "polygonID";
    private static final String FIELD_POLYGONES_SIZE = "size";

    private static final String TABLE_COORDS_NAME = "coords";
    private static final String FIELD_COORDS_COORDID = "coordID";
    private static final String FIELD_COORDS_POLYGONID = "polygonID";
    private static final String FIELD_COORDS_LAT = "lat";
    private static final String FIELD_COORDS_LON = "lon";
    private static final String FIELD_COORDS_POSITION = "position";

    private static final String TABLE_NODES_NAME = "nodes";
    private static final String FIELD_NODES_NODEID = "nodeID";
    private static final String FIELD_NODES_BUILDINGID = "buildingID";
    private static final String FIELD_NODES_LAT = "lat";
    private static final String FIELD_NODES_LON = "lon";

    private static final String TABLE_CACHED_GRID_NAME = "cached_grid";
    private static final String FIELD_CACHED_GRID_LAT = "lat";
    private static final String FIELD_CACHED_GRID_LON = "lon";
    private static final String FIELD_CACHED_GRID_USED = "used";
    private static final String FIELD_CACHED_GRID_TIMESTAMP = "timestamp";

    private static final String TABLE_COLLECTED_BUILDINGS_NAME = "collected_buildings";
    private static final String FIELD_COLLECTED_BUILDINGS_BUILDINGID = "buildingID";
    private static final String FIELD_COLLECTED_BUILDINGS_TIMESTAMP = "timestamp";

    private static final String DEFAULT_CHARSET = "utf-8";
    private static final String DEFAULT_COLLATE = "utf8_bin";

    //TODO: convert these Values into config options!
    //private static final int GRID_CACHE_MAX_TIMEDIFFERENCE = 60 * 60 * 24 *7; //Daten dürfen maximal 1 Woche alt sein!
    private static final int GRID_CACHE_MAX_ENTRYS = 500; //max amount of entrys in table grid_cache, if more entrys are present, delete them!
    private static final int COLLECTED_BUILDINGS_COLLECT_TIME = 60 * 60 * 6; //how long does a building is threated as collected
    //public static final String TABLE_images = "images";

    public LocalDBHandler(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public int activeDatabaseCount = 0;
    public synchronized SQLiteDatabase openDatabase() {
        SQLiteDatabase connection = getWritableDatabase(); // always returns the same connection instance
        activeDatabaseCount++;
        return connection;
    }
    public synchronized void closeDatabase(SQLiteDatabase connection) {
        activeDatabaseCount--;
        if (activeDatabaseCount == 0) {
            if (connection != null) {
                if (connection.isOpen()) {
                    connection.close();
                }
            }
        }
    }



    @Override
    public void onCreate(SQLiteDatabase db){
        String query;

        //Create Table resources
        query = "CREATE TABLE `" + TABLE_RESOURCES_NAME + "` (" +
                "`" + FIELD_RESOURCES_RESOURCEID + "` int(5) NOT NULL," +
                "`" + FIELD_RESOURCES_NAME + "` varchar(100) NOT NULL," +
                "`" + FIELD_RESOURCES_IMAGEID + "` int(10) NOT NULL," +
                "PRIMARY KEY (`"+FIELD_RESOURCES_RESOURCEID+"`)" +
                ");";
        db.execSQL(query);

        //Create Table areas
        query = "CREATE TABLE `" + TABLE_AREAS_NAME + "` (" +
                "`" + FIELD_AREAS_AREAID + "` int(5) NOT NULL," +
                "`" + FIELD_AREAS_NAME + "` varchar(100) NOT NULL," +
                "PRIMARY KEY (`" + FIELD_AREAS_AREAID + "`)" +
                ");";
        db.execSQL(query);

        //Create Table Images
        query = "CREATE TABLE `"+TABLE_IMAGES_NAME+"` (" +
                " `"+FIELD_IMAGES_IMAGEID+"` int(10) NOT NULL," +
                " `"+FIELD_IMAGES_IMAGE+"` blob NOT NULL," +
                " PRIMARY KEY (`"+FIELD_IMAGES_IMAGEID+"`)" +
                ");";
        db.execSQL(query);

        //Create Table buildings
        query = "CREATE TABLE `"+TABLE_BUILDINGS_NAME+"` (" +
                "`"+FIELD_BUILDINGS_BUILDINGID+"` int(5) NOT NULL," +
                "`"+FIELD_BUILDING_IMAGEID+"` int(10) NOT NULL," +
                "PRIMARY KEY (`"+FIELD_BUILDINGS_BUILDINGID+"`)" +
                ");";
        db.execSQL(query);

        //Create Table building_contains_resource
        query = "CREATE TABLE `"+TABLE_BUILDINGCONTAINSRESOURCE_NAME+"` (" +
                "`"+FIELD_BUILDINGCONTAINSRESOURCE_BUILDINGID+"` int(5) NOT NULL," +
                "`"+FIELD_BUILDINGCONTAINSRESOURCE_RESOURCEID+"` int(5) NOT NULL," +
                "PRIMARY KEY (`"+FIELD_BUILDINGCONTAINSRESOURCE_BUILDINGID+"`,`"+FIELD_BUILDINGCONTAINSRESOURCE_RESOURCEID+"`)" +
                ");";
        db.execSQL(query);

        //Create Table area_contains_resoruce
        query = "CREATE TABLE `"+TABLE_AREACONTAINSRESOURCE_NAME+"` (" +
                "`"+FIELD_AREACONTAINSRESOURCE_AREAID+"` int(5) NOT NULL," +
                "`"+FIELD_AREACONTAINSRESOURCE_RESOURCEID+"` int(5) NOT NULL," +
                "PRIMARY KEY (`"+FIELD_AREACONTAINSRESOURCE_AREAID+"`,`"+FIELD_AREACONTAINSRESOURCE_RESOURCEID+"`)" +
                ");";
        db.execSQL(query);

        //Create Table polygones
        query = "CREATE TABLE `"+TABLE_POLYGONES_NAME+"` (" +
                "`"+FIELD_POLYGONES_POLYGONID+"` int(5) NOT NULL," +
                "`"+FIELD_POLYGONES_AREAID+"` int(5) NOT NULL," +
                "`"+FIELD_POLYGONES_SIZE+"` int(5) NOT NULL," +
                "PRIMARY KEY (`"+FIELD_POLYGONES_POLYGONID+"`)" +
                ");";
        db.execSQL(query);

        //Create Table coords
        query = "CREATE TABLE `"+TABLE_COORDS_NAME+"` (" +
                "`"+FIELD_COORDS_COORDID+"` int(20) NOT NULL," +
                "`"+FIELD_COORDS_POLYGONID+"` int(20) NOT NULL," +
                "`"+FIELD_COORDS_LAT+"` float NOT NULL," +
                "`"+FIELD_COORDS_LON+"` float NOT NULL," +
                "`"+FIELD_COORDS_POSITION+ "` int(3) NOT NULL, " +
                "PRIMARY KEY (`"+FIELD_COORDS_COORDID+"`)" +
                ");";
        db.execSQL(query);

        //Create Table nodes
        query = "CREATE TABLE `"+TABLE_NODES_NAME+"` (" +
                " `"+FIELD_NODES_NODEID+"` int(20) NOT NULL," +
                " `"+FIELD_NODES_BUILDINGID+"` int(20) NOT NULL," +
                " `"+FIELD_NODES_LAT+"` float NOT NULL," +
                " `"+FIELD_NODES_LON+"` float NOT NULL," +
                " PRIMARY KEY (`"+FIELD_NODES_NODEID+"`)" +
                ");";
        db.execSQL(query);

        //Create Table cached_grids
        query = "CREATE TABLE `" + TABLE_CACHED_GRID_NAME + "` (" +
                " `"+FIELD_CACHED_GRID_LAT+"` float NOT NULL," +
                " `"+FIELD_CACHED_GRID_LON+"` float NOT NULL," +
                " `"+FIELD_CACHED_GRID_TIMESTAMP+"` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " `"+FIELD_CACHED_GRID_USED+"` int(5) NOT NULL DEFAULT '0'," +
                " PRIMARY KEY (`"+FIELD_CACHED_GRID_LAT+"`,`"+FIELD_CACHED_GRID_LON+"`)" +
                ");";
        db.execSQL(query);

        //create Table buildings_collected
        query = "CREATE TABLE `"+ TABLE_COLLECTED_BUILDINGS_NAME + "` (" +
                " `"+FIELD_COLLECTED_BUILDINGS_BUILDINGID+"` int NOT NULL," +
                " `"+FIELD_COLLECTED_BUILDINGS_TIMESTAMP+"` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " PRIMARY KEY (`"+FIELD_COLLECTED_BUILDINGS_BUILDINGID +"`)" +
                ");";
        db.execSQL(query);


        //Insert dummy values

        for (int id = 0; id<=20;id++) {
            ContentValues values = new ContentValues();
            values.put(FIELD_BUILDINGS_BUILDINGID, id);
            values.put(FIELD_BUILDING_IMAGEID, id);
            db.insertWithOnConflict(TABLE_BUILDINGS_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String query = "DROP TABLE IF EXISTS images";
        db.execSQL(query);
        onCreate(db);
    }

    // ALL getData Methods:
    public HashMap<String,Object> getGridCached(double lat, double lon, float size){
        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();

        //Placeholder for respond results
        ArrayList nodes = new ArrayList<>();

        String query =  "SELECT n." + FIELD_NODES_NODEID + ", n." + FIELD_NODES_LAT + ", n." + FIELD_NODES_LON + ", b." + FIELD_BUILDING_IMAGEID + ", b." + FIELD_BUILDINGS_BUILDINGID +
                        " FROM " + TABLE_NODES_NAME + " n, " + TABLE_BUILDINGS_NAME+" b WHERE " +
                        "(( n." + FIELD_NODES_LAT + " >= ?" +
                        " AND n." + FIELD_NODES_LAT + " <= ?" +
                        ") AND ( n." + FIELD_NODES_LON + " >= ?" +
                        " AND n." + FIELD_NODES_LON + " <= ?)" +
                        " AND (n."+FIELD_NODES_BUILDINGID + " = b." + FIELD_BUILDINGS_BUILDINGID + "))";


        String[] selectionArgs = {
                Double.toString(lat),
                Double.toString(lat+size),
                Double.toString(lon),
                Double.toString(lon+size)};

        Cursor cursor = db.rawQuery(query,selectionArgs);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            //temp hash map for singe entry
            HashMap<String,Object> node = new HashMap<>();
            //put all content to the new object

            node.put("nodeID", cursor.getLong(cursor.getColumnIndex(FIELD_NODES_NODEID)));
            node.put("lat", cursor.getDouble(cursor.getColumnIndex(FIELD_NODES_LAT)));
            node.put("lon",cursor.getDouble(cursor.getColumnIndex(FIELD_NODES_LON)));
            node.put("buildingID",cursor.getInt(cursor.getColumnIndex(FIELD_BUILDINGS_BUILDINGID)));
            node.put("imageID",cursor.getLong(cursor.getColumnIndex(FIELD_BUILDING_IMAGEID)));

            //add entry to entry list
            nodes.add(node);
        }


        //Get coords and polygones
        HashMap<Long,HashMap<String,Object>> polygones = new HashMap<>();

        //save polygone Id's for next requestblock
        //ArrayList<Long> polygoneIDs = new ArrayList<>();

        String[] projection2 = {
                FIELD_COORDS_COORDID,
                FIELD_COORDS_LAT,
                FIELD_COORDS_LON,
                FIELD_COORDS_POLYGONID,
                FIELD_COORDS_POSITION
        };
        String selection = "(("+FIELD_COORDS_LAT+" >= ? AND "+FIELD_COORDS_LAT+" <= ?) AND"+
                " ("+FIELD_COORDS_LON+" >= ? AND "+FIELD_COORDS_LON+" <= ?)) ORDER BY "+FIELD_COORDS_POLYGONID+", "+FIELD_COORDS_POSITION+" ASC";

        cursor = db.query(TABLE_COORDS_NAME,projection2,selection,selectionArgs,null,null,null);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            //temp hash map for singe entry
            HashMap<String,Object> coord = new HashMap<>();
            //put all content to the new object

            coord.put("coordID", cursor.getLong(cursor.getColumnIndex(FIELD_COORDS_COORDID)));
            coord.put("lat", cursor.getDouble(cursor.getColumnIndex(FIELD_COORDS_LAT)));
            coord.put("lon",cursor.getDouble(cursor.getColumnIndex(FIELD_COORDS_LON)));
            coord.put("pos",cursor.getInt(cursor.getColumnIndex(FIELD_COORDS_POSITION)));
            long polygonid = cursor.getLong(cursor.getColumnIndex(FIELD_COORDS_POLYGONID));

            //save polygonIDs for polygone:
            if (!polygones.containsKey(polygonid)){
                //Create Array for coords
                ArrayList<HashMap<String,Object>> polyCoords = new ArrayList<>();
                //Add current coord to list
                polyCoords.add(coord);

                //Add HashMap for different settings of this polygone, will be extended later!
                HashMap<String,Object> polygonAttributes = new HashMap<>();
                polygonAttributes.put("coords",polyCoords);
                polygonAttributes.put("polygonID",polygonid);

                //Add polygon to list
                polygones.put(polygonid,polygonAttributes);
            }else{
                //Update Polygone with current coord
                HashMap<String,Object> polygonAttributes = polygones.get(polygonid);
                ArrayList<HashMap<String,Object>> polyCoords = (ArrayList<HashMap<String,Object>>) polygonAttributes.get("coords");
                //Add current coord to list
                polyCoords.add(coord);
                polygonAttributes.put("coords",polyCoords);

                //Add polygon to list
                polygones.put(polygonid,polygonAttributes);
            }
        }


        //Get polygones
        String[] projection3 = {
                FIELD_POLYGONES_POLYGONID,
                FIELD_POLYGONES_SIZE,
                FIELD_POLYGONES_AREAID
        };

        for (long polygonid:polygones.keySet()){
            selection = "? = "+FIELD_POLYGONES_POLYGONID;

            String[] selectionArgs2 = {Long.toString(polygonid)};

            cursor = db.query(TABLE_POLYGONES_NAME,projection3,selection,selectionArgs2,null,null,null);

            //temp hash map for singe entry
            //HashMap<String,Object> polygon = new HashMap<>();
            //put all content to the new object

            cursor.moveToFirst();

            if(cursor.getCount()==0){
                Log.w(tag,"PolygonID '"+Long.toString(polygonid)+"' is out of range!");
                continue;
            }

            //get polygon reference
            HashMap<String,Object> polygonAttributes = polygones.get(polygonid);

            polygonAttributes.put("polygonID",cursor.getLong(cursor.getColumnIndex(FIELD_POLYGONES_POLYGONID)));
            polygonAttributes.put("size",cursor.getLong(cursor.getColumnIndex(FIELD_POLYGONES_SIZE)));
            polygonAttributes.put("areaID",cursor.getLong(cursor.getColumnIndex(FIELD_POLYGONES_AREAID)));

            polygones.put(polygonid,polygonAttributes);
        }


        //building result and returning
        HashMap<String,Object> result = new HashMap<>();
        result.put("nodes",nodes);
        result.put("polygones",polygones);

        closeDatabase(db);
        return result;
    }
    public Bitmap getImage(long id){
        SQLiteDatabase db = this.openDatabase();

        String[] projection = {
          FIELD_IMAGES_IMAGE
        };

        String selection = FIELD_IMAGES_IMAGEID+" = ?";
        String[] selectionArgs = {
                Long.toString(id)
        };

        Cursor cursor = db.query(TABLE_IMAGES_NAME,projection,selection,selectionArgs,null,null,null);
        if (cursor.getCount() == 0){
            Log.w(tag,"ImageID '"+Long.toString(id)+"' is out of range!");
            return null;
        }
        cursor.moveToFirst();
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(FIELD_IMAGES_IMAGE));
        ByteArrayInputStream imageStream = new ByteArrayInputStream(blob);
        Bitmap theImage = BitmapFactory.decodeStream(imageStream);

        return theImage;


    }
    //ALL isData Methods:
    public boolean isGridCached(double lat,double lon,float size){
        //get the current timestamp for compairison
        Long timestamp = System.currentTimeMillis()/1000;

        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();

        //Projection
        String[] projection = {
                FIELD_CACHED_GRID_TIMESTAMP
        };

        //Selection
        String selection = FIELD_CACHED_GRID_LAT + " = ? AND " + FIELD_CACHED_GRID_LON + " = ?";
        String[] selectionArgs = { String.valueOf(lat), String.valueOf(lon)};

        //Cursor
        Cursor c = db.query(TABLE_CACHED_GRID_NAME,projection,selection,selectionArgs,null,null,null);

        //get the first value
        c.moveToFirst();
        if (c.getCount() == 0){
            //there is no entry for this grid, Grid is not jet cached!
            closeDatabase(db);
            return false;
        }
        /*
        else{
            if(c.getLong(c.getColumnIndex(FIELD_CACHED_GRID_TIMESTAMP)) <= (timestamp - GRID_CACHE_MAX_TIMEDIFFERENCE)){
                //timestamp ist abgelaufen, Gridentry muss geloescht werden
                this.deleteGridCache(lat,lon,size);
                closeDatabase(db);
                return false;
            }
        }*/
        closeDatabase(db);
        return true;
    }
    public boolean isBuildingCollected(long buildingID){
        //get the current timestamp for compairison
        Long timestamp = System.currentTimeMillis()/1000;
        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();

        String selection = FIELD_COLLECTED_BUILDINGS_BUILDINGID+"= ?";
        String[] selectionArgs = {Long.toString(buildingID)};
        String[] projection = {FIELD_COLLECTED_BUILDINGS_TIMESTAMP};

        Cursor cursor = db.query(TABLE_COLLECTED_BUILDINGS_NAME,projection,selection,selectionArgs,null,null,null);

        cursor.moveToFirst();

        if (cursor.getCount() == 0){
            return false;
        }
        else {
            long dataTimestamp = cursor.getLong(cursor.getColumnIndex(FIELD_COLLECTED_BUILDINGS_TIMESTAMP));

            if(dataTimestamp<=(timestamp-COLLECTED_BUILDINGS_COLLECT_TIME)){
                return true;
            }
            else {
                deleteBuildingCollected(buildingID);
                return false;
            }
        }
    }

    //ALL insertData Methos:
    public void insertGridCache(double lat, double lon,HashMap<String,Object> result){
        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();
        ContentValues values = new ContentValues();


        //Insert Nodes into DataBase
        ArrayList<HashMap> nodes = (ArrayList) result.get("nodes");
        for (HashMap < String, Object > hashMap: nodes){
            values.clear();

            values.put(FIELD_NODES_NODEID,(long) hashMap.get("nodeID"));
            values.put(FIELD_NODES_BUILDINGID,(long) hashMap.get("buildingID"));
            values.put(FIELD_NODES_LAT,(double) hashMap.get("lat"));
            values.put(FIELD_NODES_LON,(double) hashMap.get("lon"));

            //write nodes, ignore errors, nodes might be inserted more than once
            db.insertWithOnConflict(TABLE_NODES_NAME,null,values,SQLiteDatabase.CONFLICT_IGNORE);
        }

        //Insert Coords into DataBase
        ArrayList<HashMap> coords = (ArrayList) result.get("coords");
        for (HashMap < String, Object > hashMap: coords){
            values.clear();

            values.put(FIELD_COORDS_COORDID,(long) hashMap.get("coordID"));
            values.put(FIELD_COORDS_LAT,(double) hashMap.get("lat"));
            values.put(FIELD_COORDS_LON,(double) hashMap.get("lon"));
            values.put(FIELD_COORDS_POLYGONID,(long) hashMap.get("polygonID"));
            values.put(FIELD_COORDS_POSITION,(int) hashMap.get("position"));

            //write nodes, ignore errors, nodes might be inserted more than once
            db.insertWithOnConflict(TABLE_COORDS_NAME,null,values,SQLiteDatabase.CONFLICT_IGNORE);
        }

        //Insert Polygones into DataBase
        ArrayList<HashMap> polygones = (ArrayList) result.get("polygones");
        for (HashMap < String, Object > hashMap: polygones){
            values.clear();

            values.put(FIELD_POLYGONES_POLYGONID,(long) hashMap.get("polygonID"));
            values.put(FIELD_POLYGONES_AREAID,(long) hashMap.get("areaID"));
            values.put(FIELD_POLYGONES_SIZE,(long) hashMap.get("size"));

            //write nodes, ignore errors, nodes might be inserted more than once
            db.insertWithOnConflict(TABLE_POLYGONES_NAME,null,values,SQLiteDatabase.CONFLICT_IGNORE);
        }


        //get the current timestamp for compairison
        Long timestamp = System.currentTimeMillis()/1000;

        values.clear();
        values.put(FIELD_CACHED_GRID_TIMESTAMP,timestamp);
        values.put(FIELD_CACHED_GRID_LAT,lat);
        values.put(FIELD_CACHED_GRID_LON,lon);
        values.put(FIELD_CACHED_GRID_USED,0);

        //Update the Grid
        db.insertWithOnConflict(TABLE_CACHED_GRID_NAME,null,values,SQLiteDatabase.CONFLICT_IGNORE);
    }
    public void insertImage(byte[] image, long id){
        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();
        ContentValues values = new ContentValues();

        values.put(FIELD_IMAGES_IMAGE,image);
        values.put(FIELD_IMAGES_IMAGEID,id);

        db.insertWithOnConflict(TABLE_IMAGES_NAME,null,values,SQLiteDatabase.CONFLICT_IGNORE);
    }
    public void insertCollectedBuilding(long buildingID){
        //get the current timestamp for compairison
        Long timestamp = System.currentTimeMillis()/1000;
        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_COLLECTED_BUILDINGS_BUILDINGID,buildingID);
        contentValues.put(FIELD_COLLECTED_BUILDINGS_TIMESTAMP,timestamp);

        db.insertWithOnConflict(TABLE_COLLECTED_BUILDINGS_NAME,null,contentValues,SQLiteDatabase.CONFLICT_IGNORE);

        closeDatabase(db);
    }

    //ALL deleteData Methos:
    public void deleteGridCache(double lat,double lon, float size){
        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();

        String selection;
        String[] selectionArgs = {
                Double.toString(lat),
                Double.toString(lat+size),
                Double.toString(lon),
                Double.toString(lon+size)};


        //delete from Nodes
        selection = "(" +FIELD_NODES_LAT+" >= ? AND "+FIELD_NODES_LAT+" <= ?)"+" AND "+
                "(" +FIELD_NODES_LON+" >= ? AND "+FIELD_NODES_LON+" <= ?)";
        db.delete(TABLE_NODES_NAME,selection,selectionArgs);


        //delete from Coords
        /*selection = "(" +FIELD_COORDS_LAT+" >= ? AND "+FIELD_COORDS_LAT+" <= ?)"+
                "(" +FIELD_COORDS_LON+" >= ? AND "+FIELD_COORDS_LON+" <= ?)";
        db.delete(TABLE_COORDS_NAME,selection,selectionArgs);
        */
        //delete from Polygones
        /*selection = "(" +FIELD_COORDS_LAT+" >= ? AND "+FIELD_COORDS_LAT+" <= ?)"+
                "(" +FIELD_COORDS_LON+" >= ? AND "+FIELD_COORDS_LON+" <= ?)";
        db.delete(FIELD_COORDS_NAME,selection,selectionArgs);
        */
        //FIXME: delete polygones from Database where coords were deleted

        //Remove old Entry out of the Gridcache
        //Selection
        selection = FIELD_CACHED_GRID_LAT + " = ? AND " + FIELD_CACHED_GRID_LON + " = ?";
        String[] selectionArgs2 = { String.valueOf(lat), String.valueOf(lon)};

        db.delete(TABLE_CACHED_GRID_NAME,selection,selectionArgs2);

    }
    public void deleteBuildingCollected(long buildingID){
        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();

        String selection = FIELD_COLLECTED_BUILDINGS_BUILDINGID+" = ?";
        String[] selectionArgs = {Long.toString(buildingID)};

        db.delete(TABLE_COLLECTED_BUILDINGS_NAME,selection,selectionArgs);
        closeDatabase(db);
    }
    //ALL Methods only related to DataSets, without callback
    public void clearOldGridEntrys(float gridsize){
        //TODO: Method is currently unused and untested!
        //get the current timestamp for compairison
        Long timestamp = System.currentTimeMillis()/1000;

        //get the max timestamp
        Long maxtimestamp = timestamp - 0;//GRID_CACHE_MAX_TIMEDIFFERENCE;

        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();


        String selection = FIELD_CACHED_GRID_TIMESTAMP+" <= ?";
        String[] selectionArgs = {Long.toString(maxtimestamp)};
        String[] projection = {FIELD_CACHED_GRID_LAT,FIELD_CACHED_GRID_LON};

        Cursor cursor = db.query(TABLE_CACHED_GRID_NAME,projection,selection,selectionArgs,null,null,null);

        
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            double lat = cursor.getFloat(cursor.getColumnIndex(FIELD_CACHED_GRID_LAT));
            double lon = cursor.getFloat(cursor.getColumnIndex(FIELD_CACHED_GRID_LON));
            deleteGridCache(lat,lon,gridsize);
            }


        selection = " COUNT(*)";
        String[] projection1 = {"COUNT(*)"};
        Cursor cursor1 = db.query(TABLE_CACHED_GRID_NAME,projection1,selection,null,null,null,null);
        cursor1.moveToFirst();

        //count of all entrys
        int entryLength = cursor1.getInt(0);

        if ( entryLength > GRID_CACHE_MAX_ENTRYS){
            Log.d(tag,"We have to remove "+entryLength+" entrys!");

            String[] projection2 = {FIELD_CACHED_GRID_TIMESTAMP,FIELD_CACHED_GRID_LAT,FIELD_CACHED_GRID_LON};

            Cursor cursor2 = db.query(TABLE_CACHED_GRID_NAME,projection2,"1=1",null,null,null,"DESC",Integer.toString(entryLength-GRID_CACHE_MAX_ENTRYS));

            for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2.moveToNext()) {
                double lat = cursor2.getFloat(cursor2.getColumnIndex(FIELD_CACHED_GRID_LAT));
                double lon = cursor2.getFloat(cursor2.getColumnIndex(FIELD_CACHED_GRID_LON));

                deleteGridCache(lat,lon,gridsize);
            }

        }


        closeDatabase(db);

    }
    public void clearOldCollectedBuildings(){
        //get the current timestamp for compairison
        Long timestamp = System.currentTimeMillis()/1000;
        //get default DataBase Handler
        SQLiteDatabase db = this.openDatabase();

        String selection = FIELD_COLLECTED_BUILDINGS_TIMESTAMP + "<= ?";
        String[] selectionArgs = {Long.toString(timestamp-COLLECTED_BUILDINGS_COLLECT_TIME)};

        db.delete(TABLE_COLLECTED_BUILDINGS_NAME,selection,selectionArgs);
    }

    //Unfinished STUFF and TEMPLATES



    public void getNode(){
        SQLiteDatabase db = this.openDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FIELD_NODES_NODEID,null
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = ""+ " = ?";
        String[] selectionArgs = { "My Title" };





    }

    public void add_images(SQL_images sql_images){
        ContentValues values = new ContentValues();
        values.put("name",sql_images.get_name());
        values.put("image",sql_images.get_image());

        SQLiteDatabase db =openDatabase();
        db.insert("images",null,values);
        db.close();
    }
}
