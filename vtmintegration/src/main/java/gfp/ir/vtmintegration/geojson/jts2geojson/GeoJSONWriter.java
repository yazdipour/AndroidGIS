/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.geojson.jts2geojson;

import org.locationtech.jts.geom.Geometry;

import java.util.List;

import gfp.ir.vtmintegration.geojson.geojson.Feature;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import gfp.ir.vtmintegration.geojson.geojson.*;
public class GeoJSONWriter {

    final static GeoJSONReader reader = new GeoJSONReader();
        
    public gfp.ir.vtmintegration.geojson.geojson.Geometry write(Geometry geometry) {
        Class<? extends Geometry> c = geometry.getClass();
        if (c.equals(Point.class)) {
            return convert((Point) geometry);
        } else if (c.equals(LineString.class)) {
            return convert((LineString) geometry);
        } else if (c.equals(Polygon.class)) {
            return convert((Polygon) geometry);
        } else if (c.equals(MultiPoint.class)) {
            return convert((MultiPoint) geometry);
        } else if (c.equals(MultiLineString.class)) {
            return convert((MultiLineString) geometry);
        } else if (c.equals(MultiPolygon.class)) {
            return convert((MultiPolygon) geometry);
        } else if (c.equals(GeometryCollection.class)) {
            return convert((GeometryCollection) geometry);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    public gfp.ir.vtmintegration.geojson.geojson.FeatureCollection write(List<Feature> features) {
        int size = features.size();
        gfp.ir.vtmintegration.geojson.geojson.Feature[] featuresJson = new gfp.ir.vtmintegration.geojson.geojson.Feature[size];
        for (int i=0; i<size; i++) {
            featuresJson[i] = features.get(i);
        }
        return new gfp.ir.vtmintegration.geojson.geojson.FeatureCollection(featuresJson);
    }

    gfp.ir.vtmintegration.geojson.geojson.Point convert(Point point) {
        gfp.ir.vtmintegration.geojson.geojson.Point json = new gfp.ir.vtmintegration.geojson.geojson.Point(
                convert(point.getCoordinate()));
        return json;
    }

    gfp.ir.vtmintegration.geojson.geojson.MultiPoint convert(MultiPoint multiPoint) {
        return new gfp.ir.vtmintegration.geojson.geojson.MultiPoint(
                convert(multiPoint.getCoordinates()));
    }

    gfp.ir.vtmintegration.geojson.geojson.LineString convert(LineString lineString) {
        return new gfp.ir.vtmintegration.geojson.geojson.LineString(
                convert(lineString.getCoordinates()));
    }

    gfp.ir.vtmintegration.geojson.geojson.MultiLineString convert(MultiLineString multiLineString) {
        int size = multiLineString.getNumGeometries();
        double[][][] lineStrings = new double[size][][];
        for (int i = 0; i < size; i++) {
            lineStrings[i] = convert(multiLineString.getGeometryN(i).getCoordinates());
        }
        return new gfp.ir.vtmintegration.geojson.geojson.MultiLineString(lineStrings);
    }

    gfp.ir.vtmintegration.geojson.geojson.Polygon convert(Polygon polygon) {
        int size = polygon.getNumInteriorRing() + 1;
        double[][][] rings = new double[size][][];
        rings[0] = convert(polygon.getExteriorRing().getCoordinates());
        for (int i = 0; i < size - 1; i++) {
            rings[i + 1] = convert(polygon.getInteriorRingN(i).getCoordinates());
        }
        return new gfp.ir.vtmintegration.geojson.geojson.Polygon(rings);
    }

    gfp.ir.vtmintegration.geojson.geojson.MultiPolygon convert(MultiPolygon multiPolygon) {
        int size = multiPolygon.getNumGeometries();
        double[][][][] polygons = new double[size][][][];
        for (int i = 0; i < size; i++) {
            polygons[i] = convert((Polygon) multiPolygon.getGeometryN(i)).getCoordinates();
        }
        return new gfp.ir.vtmintegration.geojson.geojson.MultiPolygon(polygons);
    }

    gfp.ir.vtmintegration.geojson.geojson.GeometryCollection convert(GeometryCollection gc) {
        int size = gc.getNumGeometries();
        gfp.ir.vtmintegration.geojson.geojson.Geometry[] geometries = new gfp.ir.vtmintegration.geojson.geojson.Geometry[size];
        for (int i = 0; i < size; i++) {
            geometries[i] = write((Geometry) gc.getGeometryN(i));
        }
        return new gfp.ir.vtmintegration.geojson.geojson.GeometryCollection(geometries);
    }

    double[] convert(Coordinate coordinate) {
        if(Double.isNaN( coordinate.z )) {
            return new double[] { coordinate.x, coordinate.y };
        }
        else {
            return new double[] { coordinate.x, coordinate.y, coordinate.z };
        }
    }

    double[][] convert(Coordinate[] coordinates) {
        double[][] array = new double[coordinates.length][];
        for (int i = 0; i < coordinates.length; i++) {
            array[i] = convert(coordinates[i]);
        }
        return array;
    }
}
