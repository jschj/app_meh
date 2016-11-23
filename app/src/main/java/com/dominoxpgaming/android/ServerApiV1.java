package com.dominoxpgaming.android;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

import static com.dominoxpgaming.android.mutilitys.ConvertUnits.decodeBase64Profile;
import static com.dominoxpgaming.android.mutilitys.ConvertUnits.getBytesFromBitmapPNG;


/**
 * Created by Jan on 19.09.2016.
 */

public class ServerApiV1 {

    private final static String TAG = "ServerAPIV1";

    //Creating JSON Parser object
    //JSONParser jsonParser = new JSONParser();

    //Server Api Url
    private final static String SERVER_BASEURL          = "http://euve77844.serverprofi24.de";
    private final static int    SERVER_PORT             = 81;
    private final static String SERVER_BASEAPI_PATH     = "/projectmeh/api/";
    private final static String SERVER_API_SPECIFIC_URL = "v1/index.php";
    //Final Server URL
    private final static String SERVER_FULL_API_PATH    = SERVER_BASEURL + ":" + SERVER_PORT + SERVER_BASEAPI_PATH + SERVER_API_SPECIFIC_URL;

    //JSON Nodes names
    private static final String TAG_ERROR       = "error";
    private static final String TAG_MESSAGE     = "message";
    private static final String TAG_NODES       = "nodes";
    private static final String TAG_IMAGES       = "images";
    private static final String TAG_COORDS      = "coords";
    private static final String TAG_POLYGONES   = "polygones";

    //misc tags
    private static final String TAG_LAT             = "lat";
    private static final String TAG_LON             = "lon";
    private static final String TAG_SIZE            = "size";
    private static final String TAG_IMAGE           = "image";
    private static final String TAG_NEXT_PAGE       = "nextPage";

    //IDS
    private static final String TAG_NODE_ID         = "nID";
    private static final String TAG_BUILDING_ID     = "bID";
    private static final String TAG_COORD_ID        = "cID";
    private static final String TAG_POLYGON_ID      = "pID";
    private static final String TAG_AREA_ID         = "aID";
    private static final String TAG_IMAGE_ID        = "iid";
    private static final String TAG_POSITION        = "pos";

    //Get HTTPHandler
    private static final String REQUEST_GET     = "GET";
    private static final String REQUEST_POST    = "POST";
    private static final String REQUEST_DELETE  = "DELETE";
    private static final String REQUEST_PUT     = "PUT";

    private HttpHandler httpHandler = new HttpHandler();

    public HashMap getMapTileInfo(double lat, double lon){
        //use the default size parameters
        return getMapTileInfo(lat,lon,0.01,0.01);
    }

    public HashMap getMapTileInfo(double lat, double lon, double sizex, double sizey){
        /*API Options and parameters:
        API-URL: /getMapTileInfo?lat=%&lon=%&sizex=%&sizey=%
        PARAMETERS: lat,lon,*sizex,*sizey
         */
        String url = SERVER_FULL_API_PATH+
                "/getMapTileInfo?lat=" + lat +
                "&lon=" + lon +
                "&sizex=" + sizex +
                "&sizey=" + sizey;

        // Making a request to url and getting response
        String jsonStr = httpHandler.makeServerRequest(url,REQUEST_GET);

        if (jsonStr == null){
            return null;
        }

        //Placeholder for respond results
        ArrayList nodes = new ArrayList<>();
        ArrayList coords = new ArrayList<>();
        ArrayList polygones = new ArrayList<>();

        Log.d(TAG, "Response from url: " + jsonStr);

                    if (jsonStr != null){
                        try {
                            JSONObject jsonObj = new JSONObject(jsonStr);

                            //Getting JSON Array node
                            Boolean error = jsonObj.getBoolean(TAG_ERROR);

                            if (error){
                                //we got an error message from the Server
                                Log.e(TAG, "An API Error accurred on the Server: "+jsonObj.getString(TAG_MESSAGE));
                                return null;
                            }
                            else{

                    //Nodes
                    JSONArray jnodes = jsonObj.getJSONArray(TAG_NODES);
                    for (int i = 0; i < jnodes.length(); i++){
                        JSONObject jnode = jnodes.getJSONObject(i);

                        //temp hash map for singe entry
                        HashMap<String,Object> node = new HashMap<>();
                        //put all content to the new object
                        node.put("nodeID",jnode.getLong(TAG_NODE_ID));
                        node.put("lat",jnode.getDouble(TAG_LAT));
                        node.put("lon",jnode.getDouble(TAG_LON));
                        node.put("buildingID",jnode.getLong(TAG_BUILDING_ID));
                        //add entry to entry list
                        nodes.add(node);
                    }

                    //Coords
                    JSONArray jcoords = jsonObj.getJSONArray(TAG_COORDS);
                    for (int i = 0; i < jcoords.length(); i++){
                        JSONObject jcoord = jcoords.getJSONObject(i);

                        //temp hash map for singe entry
                        HashMap<String,Object> coord = new HashMap<>();
                        //put all content to the new object
                        coord.put("coordID",jcoord.getLong(TAG_COORD_ID));
                        coord.put("lat",jcoord.getDouble(TAG_LAT));
                        coord.put("lon",jcoord.getDouble(TAG_LON));
                        coord.put("polygonID",jcoord.getLong(TAG_POLYGON_ID));
                        coord.put("position",jcoord.getInt(TAG_POSITION));
                        //add entry to entry list
                        coords.add(coord);
                    }

                    //Polygones
                    JSONArray jpolygones = jsonObj.getJSONArray(TAG_POLYGONES);
                    for (int i = 0; i < jpolygones.length(); i++){
                        JSONObject jpolygone = jpolygones.getJSONObject(i);

                        //temp hash map for singe entry
                        HashMap<String,Object> polygon = new HashMap<>();
                        //put all content to the new object
                        polygon.put("polygonID",jpolygone.getLong(TAG_POLYGON_ID));
                        polygon.put("areaID",jpolygone.getLong(TAG_AREA_ID));
                        polygon.put("size",jpolygone.getLong(TAG_SIZE));
                        //add entry to entry list
                        polygones.add(polygon);
                    }
                }
            }
            catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }
        }
        else {
            Log.e(TAG, "Couldn't get json from server.");
        }

        //building result and returning
        HashMap<String,Object> result = new HashMap<>();
        result.put("nodes",nodes);
        result.put("coords",coords);
        result.put("polygones",polygones);

        return result;
    }

    public HashMap getImages(int page) {
        /*API Options and parameters:
        API-URL: /getImages?page=%
        PARAMETERS: page
         */
        String url = SERVER_FULL_API_PATH + "/getImages?page=" + page;

        // Making a request to url and getting response
        String jsonStr = httpHandler.makeServerRequest(url, REQUEST_GET);

        if (jsonStr == null) {
            return null;
        }

        //Placeholder for respond results
        ArrayList<HashMap<String, Object>> images = new ArrayList<>();
        int nextPage = -1;

        Log.d(TAG, "Response from url: " + jsonStr);

        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                //Getting JSON Array node
                Boolean error = jsonObj.getBoolean(TAG_ERROR);

                nextPage = jsonObj.getInt(TAG_NEXT_PAGE);

                if (error) {
                    //we got an error message from the Server
                    Log.e(TAG, "An API Error accurred on the Server: " + jsonObj.getString(TAG_MESSAGE));
                    return null;
                } else {

                    //Nodes
                    JSONArray jnodes = jsonObj.getJSONArray(TAG_IMAGES);
                    for (int i = 0; i < jnodes.length(); i++) {
                        JSONObject jnode = jnodes.getJSONObject(i);

                        //temp hash map for singe entry
                        HashMap<String, Object> image = new HashMap<>();
                        //put all content to the new object
                        image.put("imageID", jnode.getLong(TAG_IMAGE_ID));

                        String img = jnode.getString(TAG_IMAGE);
                        //convert imagestring to
                        byte[] convertedImage = getBytesFromBitmapPNG(decodeBase64Profile(img));
                        //add converted Image
                        image.put("image",convertedImage);

                        //image.put("image", );
                        //add entry to entry list
                        images.add(image);
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }
        }

        //building result and returning
        HashMap<String,Object> result = new HashMap<>();
        result.put("images",images);
        result.put("nextPage",nextPage);

        return result;
    }

}

