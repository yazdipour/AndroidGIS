/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm;

import android.content.Context;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.VectorLayer;
import org.oscim.layers.vector.geometries.JtsDrawable;
import org.oscim.layers.vector.geometries.LineDrawable;
import org.oscim.layers.vector.geometries.PointDrawable;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.ArrayList;

import gfp.ir.vtmintegration.geojson.geojson.Feature;
import gfp.ir.vtmintegration.geojson.geojson.GeoJSONFactory;
import gfp.ir.vtmintegration.geojson.jts2geojson.GeoJSONReader;

public class SearchLayer extends VectorLayer {
    private final Context context;
    private Geometry mExtent = null;
    private Style.Builder style;
    private java.util.List<JtsDrawable> jtsDrawables = new ArrayList<>();
    // parse Geometry from Feature
    private GeoJSONReader reader = new GeoJSONReader();

    public SearchLayer(Context context, Map map) {
        super(map);
        this.context = context;
    }

    /**
     * Addes a json Feature object into map. this could be used when user has selected a Feature object from a list of Feature collection
     *
     * @param Feature an string Feature object on geojson format.
     *                {
     *                "type": "Feature",
     *                "properties": {
     *                "name": "گلستان3",
     *                "pk": "559"
     *                },
     *                "geometry": {
     *                "type": "LineString",
     *                "coordinates": [
     *                [
     *                50.9827250000139,
     *                35.8387440358409
     *                ],
     *                [
     *                50.983286419352,
     *                35.8385855736983
     *                ]
     *                ]
     *                }
     *                }
     */
    public void AddFeature(String Feature) {
        try {
            gfp.ir.vtmintegration.geojson.geojson.Feature feature = (Feature) GeoJSONFactory.create(Feature);
            AddFeature(feature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.update();
    }

    /**
     * Addes a list of gemetries which are called as FeatureCollection. This could be used when a list directly loads from server
     *
     * @param FeatureCollection {
     *                          "type": "FeatureCollection",
     *                          "crs": {
     *                          "type": "name",
     *                          "properties": {
     *                          "name": "EPSG:4326"
     *                          }
     *                          },
     *                          "features": [
     *                          {
     *                          "type": "Feature",
     *                          "properties": {
     *                          "name": "گلستان2",
     *                          "pk": "562"
     *                          },
     *                          "geometry": {
     *                          "type": "LineString",
     *                          "coordinates": [
     *                          [
     *                          50.983077535276,
     *                          35.8381014815964
     *                          ],
     *                          [
     *                          50.9836228241265,
     *                          35.8379483468643
     *                          ]
     *                          ]
     *                          }
     *                          },
     *                          {
     *                          "type": "Feature",
     *                          "properties": {
     *                          "name": "گلستان1",
     *                          "pk": "561"
     *                          },
     *                          "geometry": {
     *                          "type": "LineString",
     *                          "coordinates": [
     *                          [
     *                          50.9825235418769,
     *                          35.8382611745661
     *                          ],
     *                          [
     *                          50.983077535276,
     *                          35.8381014815964
     *                          ]
     *                          ]
     *                          }
     *                          },
     *                          {
     *                          "type": "Feature",
     *                          "properties": {
     *                          "name": "گلستان4",
     *                          "pk": "560"
     *                          },
     *                          "geometry": {
     *                          "type": "LineString",
     *                          "coordinates": [
     *                          [
     *                          50.983286419352,
     *                          35.8385855736983
     *                          ],
     *                          [
     *                          50.9838219436289,
     *                          35.8384331147343
     *                          ]
     *                          ]
     *                          }
     *                          },
     *                          {
     *                          "type": "Feature",
     *                          "properties": {
     *                          "name": "گلستان3",
     *                          "pk": "559"
     *                          },
     *                          "geometry": {
     *                          "type": "LineString",
     *                          "coordinates": [
     *                          [
     *                          50.9827250000139,
     *                          35.8387440358409
     *                          ],
     *                          [
     *                          50.983286419352,
     *                          35.8385855736983
     *                          ]
     *                          ]
     *                          }
     *                          }
     *                          ]
     *                          }
     */
    public void AddFeatures(String FeatureCollection) {
        try {
            gfp.ir.vtmintegration.geojson.geojson.FeatureCollection featureCollection =
                    (gfp.ir.vtmintegration.geojson.geojson.FeatureCollection) GeoJSONFactory.create(FeatureCollection);
            for (gfp.ir.vtmintegration.geojson.geojson.Feature feature : featureCollection.getFeatures())
                AddFeature(feature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.update();
    }

    private void AddFeature(gfp.ir.vtmintegration.geojson.geojson.Feature feature) {
        Geometry mGeometry = reader.read(feature.getGeometry());
        AddGeometry(mGeometry, feature.getProperties());
    }

    private void AddGeometry(Geometry mGeometry, java.util.Map<String, Object> properties) {
        if (mGeometry != null && mGeometry.isValid()) {
            for (int j = 0; j < mGeometry.getNumGeometries(); j++) {
                //calculate extent
                if (mExtent == null) {
                    mExtent = mGeometry.getEnvelope();
                } else {
                    mExtent = mExtent.union(mGeometry).getEnvelope();
                }
                JtsDrawable jtsDrawable = null;
                switch (mGeometry.getGeometryType()) {
                    case "Polygon":
                    case "MultiPolygon":
                        jtsDrawable = new PolygonDrawable(mGeometry.getGeometryN(j), getStyle(properties));
                        break;
                    case "Point":
                    case "MultiPoint":
                        jtsDrawable = new PointDrawable(new GeoPoint(mGeometry.getCoordinate().x, mGeometry.getCoordinate().y), getStyle(properties));
                        break;
                    case "LineString":
                    case "MultiLineString":
                        jtsDrawable = new LineDrawable(mGeometry.getGeometryN(j), getStyle(properties));
                        break;
                }
                if (jtsDrawable != null) {
                    jtsDrawables.add(jtsDrawable);
                    this.add(jtsDrawable);
                }
            }
        }
    }

    private Style getStyle(java.util.Map<String, Object> propetries) {
        if (style == null) style = Style.builder()
                .strokeColor("#99FF00FF")
//                .strokeColor("#990DB6FC")
                .strokeWidth(4 * context.getResources().getDisplayMetrics().density)
                .fillColor("#99FF00FF")
//                .fillColor("#990DB6FC")
                .fixed(true)
                .buffer(0.000001 * context.getResources().getDisplayMetrics().density)
                .fillAlpha(1);
        return style.build();
    }

    public void setStyle(Style.Builder style) {
        this.style = style;
    }

    public void zoomToExtent(double scale) {
        if (mExtent != null)
            mMap.setMapPosition(mExtent.getCentroid().getY(), mExtent.getCentroid().getX(), scale);
    }

    public GeoPoint getCenter() {
        if (mExtent == null) return null;
        return new GeoPoint(mExtent.getCentroid().getY(), mExtent.getCentroid().getX());
    }

    public Envelope getEnvelope() {
        return mExtent.getEnvelopeInternal();
    }

    public java.util.List<JtsDrawable> getFeatures() {
        return jtsDrawables;
    }

    public void clear() {
        for (JtsDrawable g : jtsDrawables) remove(g);
        jtsDrawables.clear();
    }
}
