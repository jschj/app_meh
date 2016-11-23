package com.dominoxpgaming.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Jan on 06.10.2016.
 */

public class ImageHandler {
    //Tag for Log Print
    private static final String TAG = "ImageProvider";
    public HashMap<Integer,Drawable> cachedIcons = new HashMap<>();
    private HashMap<int[],Integer> cachedReference = new HashMap<>();

    public int DEFAULT_IMAGE_ID = 2;
    private Drawable DEFAULT_IMAGE;

    private LocalDBHandler databaseHandler;
    private Context context;

    private Boolean logAccess = false;


    public ImageHandler(LocalDBHandler databaseHandler, Context context) {
        this.databaseHandler = databaseHandler;
        this.context = context;

        DEFAULT_IMAGE = ContextCompat.getDrawable(this.context,R.drawable.erroricon);//ResourcesCompat.getDrawable(context.getResources(),R.drawable.erroricon,context.getTheme());
    }



    public Drawable getCachedIcons(int IconId, int width, int height){
        int[] key = {IconId,width,height};

        int refKey = -1;
        //TODO: migh fix  'cachedReference.contains() will not work on int[]' Problem?
        //Searching manually, because cachedReference.contains() will not work on int[]

        for (int[] ref: cachedReference.keySet()){
            if(key[0] == ref[0]){
                if((key[1] == ref[1]) && (key[2]==ref[2])){
                    refKey = cachedReference.get(ref);
                    break;
                }
            }
        }

        if (refKey == -1){
            refKey = cachedIcons.size();//Latest value, not even cached (size is automatic +1), because of counting from 0
            cachedReference.put(key,refKey);
        }


        if(cachedIcons.containsKey(refKey)){
            Log.i(TAG,"Returning Cached Icon #"+Integer.toString(IconId));
            return cachedIcons.get(refKey);
        }
        else{
            while(logAccess ){
                synchronized (logAccess){
                    try{
                        // Calling wait() will block this thread until another thread
                        // calls notify() on the object.
                        logAccess.wait();
                    }catch (InterruptedException e){
                        //Happens if someone interrupts your thread.
                    }
                }

                logAccess = true;
            }


            Log.d(TAG,"Inserting Cached Icon #"+Integer.toString(IconId));

            //Get Bitmap or null from DataBase
            Bitmap bitMap = databaseHandler.getImage(IconId);

            if (bitMap==null){
                Log.w(TAG,"Cached Icon #"+Integer.toString(IconId)+" is not availbe?");
                //Returing Error/Default Image

                return DEFAULT_IMAGE;
                //bitMap = databaseHandler.getImage(DEFAULT_IMAGE_ID);
            }

            Bitmap resized = Bitmap.createScaledBitmap(bitMap,width,height,true);

            //Get Drawable
            Drawable draw = new BitmapDrawable(context.getResources(),resized);

            cachedIcons.put(refKey,draw);

            //unlock getmethod...
            logAccess = false;
            synchronized (logAccess){
                logAccess.notify();
            }

            return cachedIcons.get(refKey);
        }
    }

    public void flushCachedIcons(){
        Log.i(TAG,"Flushing "+cachedIcons.size()+" Cached Icons...");
        cachedIcons.clear();
        cachedReference.clear();
    }




}
