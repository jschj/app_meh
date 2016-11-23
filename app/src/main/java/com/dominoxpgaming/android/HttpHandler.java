package com.dominoxpgaming.android;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jan on 19.09.2016.
 */
public class HttpHandler {
    private static final String TAG = "HttpHandler";

    private static final String REQUEST_GET     = "GET";
    private static final String REQUEST_POST    = "POST";
    private static final String REQUEST_DELETE  = "DELETE";
    private static final String REQUEST_PUT     = "PUT";

    private static Set<String> ALLOWED_SERVER_REQUEST_METHODS = new HashSet<String>(Arrays.asList(
            new String[] {REQUEST_GET,REQUEST_POST,REQUEST_PUT,REQUEST_DELETE}
    ));

    public String makeServerRequest(String requesturl,String requestmethod){
        //Check if requestmethod is allowed
        if (!ALLOWED_SERVER_REQUEST_METHODS.contains(requestmethod)){
            Log.e(TAG,"Exception: ValueError: Server Request Method '"+requestmethod+"' unknown or not allowed!");
            return null;
        }

        String response = null;

        try{
            //Convert Url to URLObject
            URL url = new URL(requesturl);
            //Get content of given url
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(requestmethod);
            //Read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);

        }
        catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;

    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
