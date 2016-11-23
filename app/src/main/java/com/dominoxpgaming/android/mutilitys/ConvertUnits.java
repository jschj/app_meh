package com.dominoxpgaming.android.mutilitys;

/**
 * Created by Jan on 03.08.2016.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;

//found at http://stackoverflow.com/questions/4605527/converting-pixels-to-dp


public class ConvertUnits {
    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * param context Context to get resources and device specific display metrics
     * return A float value to represent px equivalent to dp depending on device density
     */

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }


    /**
     * This method converts device specific pixels to density independent pixels.
     * <p/>
     * param px A value in px (pixels) unit. Which we need to convert into db
     * param context Context to get resources and device specific display metrics
     * return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static double round(double n, int decimals) {
        return Math.floor(n * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    public static Bitmap decodeBase64Profile(String input) {
        Bitmap bitmap = null;
        if (input != null) {
            byte[] decodedByte = Base64.decode(input, 0);
            bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        }
        return bitmap;
    }

    public static byte[] getBytesFromBitmapPNG(Bitmap bitmap) {
        if (bitmap!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
            return stream.toByteArray();
        }
        return null;
    }



}