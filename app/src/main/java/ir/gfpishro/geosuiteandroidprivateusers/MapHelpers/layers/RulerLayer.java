package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.widget.TextView;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.oscim.android.MapView;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.vector.VectorLayer;
import org.oscim.layers.vector.geometries.LineDrawable;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;

import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MapHandler;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class RulerLayer extends VectorLayer {
    private final MarkerSymbol symbol;
    private final List<MarkerItem> markers = new ArrayList<>();
    private final List<GeoPoint> points = new ArrayList<>();
    private final MapHandler mapHandler;
    private int mapIndex;
    private Style styleLine, stylePolygon;
    private Snackbar snackbar;
    private PolygonDrawable polygonDrawable;
    private LineDrawable lines;

    public RulerLayer(MapView mapView, final MapHandler mapHandler, final Context context, int mapIndex, final FloatingActionButton fab) {
        super(mapView.map());
        this.mapHandler = mapHandler;
        symbol = mapHandler.markerHandler.getSymbol(R.drawable.ic_add_black_24dp, context, false);
        this.mapIndex = mapIndex;
        Style.Builder style = Style.builder()
                .strokeWidth(3 * context.getResources().getDisplayMetrics().density)
                .fillColor("#7FFFC312")
                .strokeColor("#00FFC312")
                .fixed(true)
                .buffer(0.000001 * context.getResources().getDisplayMetrics().density)
                .fillAlpha(1);
        stylePolygon = style.build();
        style.strokeColor("#FFC312");
        styleLine = style.build();
        snackbar = Snackbar.make(mapView, "", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("خروج", view -> fab.performClick());
        TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(3);
        mapHandler.markerHandler.getMarkerLayer().setOnItemGestureListener(new ItemizedLayer.OnItemGestureListener<MarkerItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, MarkerItem item) {
                if (markers.contains(item)) removeMarker(item);
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, MarkerItem item) {
                return false;
            }
        });
    }

    private void removeMarker(MarkerItem item) {
        try {
            markers.remove(item);
            mapHandler.markerHandler.removeItem(item);
            points.remove(item.getPoint());
            updateLineDrawable(points);
            updatePolygon(points);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            showCalcBar();
            this.update();
        }
    }

//    private double calculateSphere() {
//        return points.size() > 1 ? points.get(points.size() - 2).sphericalDistance(points.get(points.size() - 1)) : 0;
//    }

    private double calculateDistance() {
        if (points.size() > 1) {
            double distance = 0;
            for (int i = 0; i < points.size() - 1; i++) {
                GeoPoint p = points.get(i);
                GeoPoint pp = points.get(i + 1);
                distance += p.sphericalDistance(pp);
            }
            return distance;
        } else return 0;
    }

    private double calculateArea() {
        if (points.size() <= 2) return 0;
        Coordinate[] coordinates = new Coordinate[points.size() + 1];
        for (int i = 0; i < points.size(); i++) {
            GeoPoint p = points.get(i);
            org.oscim.core.Point point = new org.oscim.core.Point();
            p.project(point);
            coordinates[i] = new Coordinate(point.getX(), point.getY());
            if (i == 0) coordinates[points.size()] = coordinates[0];
        }
        Polygon polygon = new GeometryFactory().createPolygon(coordinates);
        polygon.setSRID(3857);
        return polygon.getArea() * 1000000000000000d;
    }

    public void addPoint(final GeoPoint p, final boolean withMarker) {
        points.add(p);
        if (withMarker) {
            MarkerItem marker = mapHandler.markerHandler.getMarkerItem(p, symbol, "ruler", "");
            mapHandler.markerHandler.addItem(marker);
            markers.add(marker);
        }
        if (points.size() > 1)
            updateLineDrawable(points);
        if (points.size() > 2)
            updatePolygon(points);
        this.update();
        showCalcBar();
    }

    private void updateLineDrawable(List<GeoPoint> points) {
        try {
            if (lines != null) this.remove(lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lines = new LineDrawable(points, styleLine);
        this.add(lines);
    }

    private void updatePolygon(List<GeoPoint> points) {
        try {
            if (polygonDrawable != null) this.remove(polygonDrawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<GeoPoint> newPoints = new ArrayList<>(points);
        newPoints.add(points.get(0));
        polygonDrawable = new PolygonDrawable(newPoints, stylePolygon);
        this.add(polygonDrawable);
    }

    private void showCalcBar() {
        snackbar.setText(String.format("فاصله بین نقاط: %s\nمساحت: %s",
                Utils.getDistanceFormatted(calculateDistance(), false),
                Utils.getDistanceFormatted(calculateArea(), true)
        ));
        snackbar.show();
    }

    public void dispose() {
        snackbar.dismiss();
        for (int i = markers.size() - 1; i >= 0; i--)
            mapHandler.getMarkerLayer().removeItem(markers.get(i));
        setEnabled(false);
        mMap.layers().remove(mapIndex);
        markers.clear();
        points.clear();
        mapHandler.markerHandler.getMarkerLayer().setOnItemGestureListener(null);
        mMap.updateMap(false);
    }
}
