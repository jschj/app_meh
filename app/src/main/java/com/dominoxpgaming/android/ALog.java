package com.dominoxpgaming.android;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;

/**
 * Created by Jan on 28.11.2016.
 *
 */

//Advanced Logger

public class ALog{
    private static final String VERBOSE = "";
    private static final String DEBUG = "";
    private static final String INFO = "";
    private static final String WARNING = "";
    private static final String EXCEPTION = "";


    public static void v(String TAG, String message){
        Log.v(TAG,message);
        log(TAG,VERBOSE,message);
    }
    public static void d(String TAG, String message){
        Log.d(TAG,message);
        log(TAG,DEBUG,message);
    }
    public static void i(String TAG, String message){
        Log.i(TAG,message);
        log(TAG,INFO,message);
    }
    public static void w(String TAG, String message){
        Log.w(TAG,message);
        log(TAG,WARNING,message);
    }
    public static void e(String TAG, String message){
        Log.e(TAG,message);
        log(TAG,EXCEPTION,message);
    }


    public static void log(String TAG, String level,String message){
        appendLog("["+level+"] TIME: "+java.util.GregorianCalendar.DAY_OF_MONTH +
                "-"+ java.util.GregorianCalendar.MONTH +" "+GregorianCalendar.HOUR_OF_DAY +":"+GregorianCalendar.MINUTE +
                ":"+GregorianCalendar.SECOND +"."+ GregorianCalendar.MILLISECOND + "    PID: "+
                android.os.Process.myPid()+ "    TID: "+android.os.Process.myTid()+ "    Application: com.dominoxpgaming.android"+
                "    TAG:" +TAG+ "    TEXT: "+message);

    }

    public static void appendLog(String text) {
        File dir = new File("/sdcard/com.dominoxpgaming.android.logs");
        if(!dir.exists()) {
            Log.d("Dir created", "Dir created ");
            dir.mkdirs();
        }


        String string = java.util.GregorianCalendar.DAY_OF_MONTH + "."+ java.util.GregorianCalendar.MONTH +"_"+GregorianCalendar.HOUR_OF_DAY+"H_";

        File logFile = new File("sdcard/com.dominoxpgaming.android.logs/latest_dump_"+string+".log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
