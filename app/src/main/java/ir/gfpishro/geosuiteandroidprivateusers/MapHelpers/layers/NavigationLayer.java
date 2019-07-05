package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers;

import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;

import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.GraphHopperHandler;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MapHandler;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MarkerHandler;

public class NavigationLayer extends PathLayer {
    public MarkerItem[] navigationMarkers = new MarkerItem[]{null, null};
    public GeoPoint[] points = new GeoPoint[2];

    public NavigationLayer(Map map, int lineColor, float lineWidth) {
        super(map, lineColor, lineWidth);
    }

    public void hide(MarkerHandler markerHandler) {
        hideMarkers(markerHandler);
        clearPath();
        setEnabled(false);
        navigationMarkers = new MarkerItem[]{null, null};
        points = new GeoPoint[2];
        map().updateMap(false);
    }

    public void hideMarkers(MarkerHandler markerHandler) {
        for (int i = navigationMarkers.length - 1; i >= 0; i--) {
            try {
                if (navigationMarkers[i] == null) continue;
                markerHandler.getMarkerLayer().removeItem(navigationMarkers[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void navigate(MapHandler mapHandler, GeoPoint point1, GeoPoint point2) throws Throwable {
        points[0] = point1;
        points[1] = point2;
        navigateFromPoints(mapHandler);
    }

    public void navigateFromPoints(MapHandler mapHandler) throws Throwable {
        if (GraphHopperHandler.getGraphHopper() == null)
            throw new NullPointerException("No GraphHopper!");
        org.oscim.layers.vector.PathLayer layer = mapHandler.offlineNavigationTo(
                points[0].getLatitude(),
                points[0].getLongitude(),
                points[1].getLatitude(),
                points[1].getLongitude());
        clearPath();
        setPoints(layer.getPoints());
        setEnabled(true);
        points = new GeoPoint[2];
    }

    public void addToMap() {
        if (!map().layers().contains(this))
            map().layers().add(this);
    }
}
