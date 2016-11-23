package com.dominoxpgaming.android.MapContent;

import org.osmdroid.views.overlay.Polygon;

/**
 * Created by Jan on 20.10.2016.
 */

public class AreaPolygone extends Polygon {
    private int areaId;

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }
}
