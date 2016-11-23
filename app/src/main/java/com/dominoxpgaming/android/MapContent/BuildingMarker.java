package com.dominoxpgaming.android.MapContent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * Created by Jan on 20.10.2016.
 */
public final class BuildingMarker extends Marker {
    private int buildingId = -1;

    public BuildingMarker(MapView mapView) {
        super(mapView);
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }
}
