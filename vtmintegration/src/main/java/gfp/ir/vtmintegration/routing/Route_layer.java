/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.routing;

import android.util.Base64;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.VectorLayer;
import org.oscim.layers.vector.geometries.LineDrawable;
import org.oscim.layers.vector.geometries.PointDrawable;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import gfp.ir.vtmintegration.geojson.geojson.Feature;
import gfp.ir.vtmintegration.geojson.geojson.GeoJSONFactory;
import gfp.ir.vtmintegration.geojson.jts2geojson.GeoJSONReader;
import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;

public class Route_layer extends VectorLayer {

    public static int SPATIALITE_VERSION=4;

    SpatialiteDatabaseHandler mSplite;
    String mNetworkname;
    String mPointtable;
    Double mCost=.00;
    Geometry mGeometry=null;
    String mNode_from;
    String mNode_to;
    int mPointTableSRID=4326;
    Style mStyle;
    private double mDistance=0;
    Coordinate distanation;
    Coordinate origin;

    /**
     *  Generates a route layer. it must be added to map later.
     * @param map map object
     * @param splite_ spatilite database handler
     * @param networkname_ netwrok name. in most of cases it could be 'roads_net'
     * @param point_table the table which contains node points. it could be 'roads'
     * @param node_from if you have already know the start point id. otherwise set it to -1 setting this parameter decreases excution time
     * @param node_to if you have already know the start point id. otherwise set it to -1 setting this parameter decreases excution time
     * @param PointTableSRID The SRID of points layer. if you set it to -1 it will not transform input point and causes higher performance but
     *                       it could make no results if the input points srid is diffrent than roads_net srid
     */
    public Route_layer(Map map,SpatialiteDatabaseHandler splite_,String networkname_,String point_table,String node_from,String node_to
    ,int PointTableSRID) {
        super(map);
        mSplite=splite_;

        mNetworkname=networkname_;
        mPointtable=point_table;
        mNode_from=node_from;
        mNode_to=node_to;
        mPointTableSRID=PointTableSRID;
        if(!checkSpatialIndexEnabled()){
            EnableSpatilaIndex();
        }


        Style.Builder sb = Style.builder()
                .strokeColor(Color.GREEN)
                .strokeWidth(3);



        mStyle = sb.build();

    }

    /**
     * Enables spatial index on table
     */
    private void EnableSpatilaIndex() {

        mSplite.enableSpatialIndex(mPointtable,"geometry");
    }

    /**
     * checks if spatial index is enabled on table
     * @return true if it is enabled
     */
    private boolean checkSpatialIndexEnabled() {

        return mSplite.SpatialIndexEnabled(mPointtable);
    }


    /**
     *  Routing using spatilite database. it is in offline mode
     * @param lon1 the longtitude of start point
     * @param lat1 the lat of start point
     * @param lon2 the longtitude of end point
     * @param lat2 the lat of end point
     */
    public void route(double lon1, double lat1, double lon2, double lat2){

        calculateCurrentDistance(lon1,lat1,lon2,lat2);
        cleanRoute();

        SpatialiteDatabaseHandler.route route= mSplite.gerRoute(mPointtable,mNetworkname,lon1,lat1,lon2,lat2,-1,-1,SPATIALITE_VERSION,mPointTableSRID,mNode_from,mNode_to);

        if(route==null)
            return;

        mCost=route.getCost_();
        mGeometry=route.getGeom_();

        AddRoute();
    }

    /**
     * calculates the initiatal eqludian distance between origin and distanation points
     * @param lon1 the longtitude of start point
     * @param lat1 the lat of start point
     * @param lon2 the longtitude of end point
     * @param lat2 the lat of end point
     */
    private void calculateCurrentDistance(double lon1, double lat1, double lon2, double lat2) {


        origin=new Coordinate(lon1,lat1);
        distanation=new Coordinate(lon2,lat2);

        mDistance=  origin.distance(distanation);

    }

    /**
     * every time a new route generates other geometries and costs must be cleared
     */
    private void cleanRoute() {
        if(mGeometry!=null)
            this.remove(mGeometry);
            mCost=0.0;
    }


    /**
     * when a route is ready we must draw them on map.
     */
    private void AddRoute(){

        if(mGeometry==null)
            return;

        if(mGeometry.isValid()) {
            for (int j = 0; j < mGeometry.getNumGeometries(); j++) {
                switch (mGeometry.getGeometryType()) {
                    case "Polygon":
                    case "MultiPolygon":
                        this.add(new PolygonDrawable(mGeometry.getGeometryN(j), getStyle()));
                        break;
                    case "Point":
                    case "MultiPoint":
                        this.add(new PointDrawable(new GeoPoint(mGeometry.getCoordinate().x,mGeometry.getCoordinate().y), getStyle()));

                        break;
                    case "LineString":
                    case "MultiLineString":
                        this.add(new LineDrawable(mGeometry.getGeometryN(j), getStyle()));
                        break;
                }
            }
        }


        this.update();
    }


    /**
     * If the route is provided by server we just need to convert it and draw it on map. it is an easy task.
     * we call this online route. (it could be geneated using google map api on server side or using pgrouting )
     * @param lon1 the longtitude of start point; does not effect the route. just to estimate costs
     * @param lat1 the lat of start point; does not effect the route. just to estimate costs
     * @param lon2 the longtitude of end point; does not effect the route. just to estimate costs
     * @param lat2 the lat of end point; does not effect the route. just to estimate costs
     * @param wkb the base64 format of Linestring wkb
     * @param Cost the total cost
     */
    public void route(double lon1, double lat1, double lon2, double lat2,String wkb,double Cost){

        byte[] data = Base64.decode(wkb, Base64.DEFAULT);
        WKBReader wkbReader = new WKBReader();
        calculateCurrentDistance(lon1,lat1,lon2,lat2);
        cleanRoute();
        try {
            mCost=Cost;
            mGeometry = wkbReader.read(data);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        AddRoute();
    }

    /**
     * draw route from a json feature. it only draws a feature
     * @param geojsonfeature json feature string
     *
     */

    public void route(double lon1, double lat1, double lon2, double lat2,String geojsonfeature,Boolean isCollection){
        try{
            calculateCurrentDistance(lon1,lat1,lon2,lat2);
            cleanRoute();

                if(isCollection){
                    gfp.ir.vtmintegration.geojson.geojson.FeatureCollection   featureCollection =
                            (gfp.ir.vtmintegration.geojson.geojson.FeatureCollection) GeoJSONFactory.create(geojsonfeature);
                    // parse Geometry from Feature
                    GeoJSONReader reader = new GeoJSONReader();
                    for (gfp.ir.vtmintegration.geojson.geojson.Feature feature:featureCollection.getFeatures()) {
                        Geometry mGeometry = reader.read(feature.getGeometry());
                        AddGeometry(mGeometry,feature.getProperties());
                    }
                }else{



                Feature feature = (Feature) GeoJSONFactory.create(geojsonfeature);
                // parse Geometry from Feature
                GeoJSONReader reader = new GeoJSONReader();
                mGeometry = reader.read(feature.getGeometry());
                mCost=mGeometry.getLength();
                 }


        }catch (Exception e){
            e.printStackTrace();
        }

        AddRoute();
    }


    private void AddGeometry(Geometry mGeometry, java.util.Map<String, Object> propetries){

            if(mGeometry==null)
                return;


            if(mGeometry.isValid()) {
                for (int j = 0; j < mGeometry.getNumGeometries(); j++) {


                    switch (mGeometry.getGeometryType()) {
                        case "Polygon":
                        case "MultiPolygon":
                            this.add(new PolygonDrawable(mGeometry.getGeometryN(j), getStyle()));
                            break;
                        case "Point":
                        case "MultiPoint":
                            this.add(new PointDrawable(new GeoPoint(mGeometry.getCoordinate().x,mGeometry.getCoordinate().y), getStyle()));

                            break;
                        case "LineString":
                        case "MultiLineString":
                            this.add(new LineDrawable(mGeometry.getGeometryN(j), getStyle()));
                            break;
                    }
                }
            }




            this.update();
    }




    /**
     * the Style of line
     * @return Style
     */
    private Style getStyle(){


        return mStyle;

    }


    /**
     * If you have a costomized style set it here. don't forget to set it before calling route functions
     * @param style
     */
    public void setStyle(Style style){
        mStyle=style;
    }

    /**
     * Returns the total cost of routing. don't forget to call it after calling route functions
     * @return total cost
     */
    public double getCost(){
        return mCost;
    }


    /**
     *  Returns the Geometry of route. don't forget to call it after calling route functions
     * @return Geometry of route.
     */
    public Geometry getGeometry(){
        return mGeometry;
    }

    /**
     * To update the cost of route (remaining distance)  ; this is not tested yet, it might return unscaled values
     * @param currentX the users current position's x coordiante
     * @param currentY the users current position's y coordiante
     * @return the remaining distance
     */
    public double getRamainedCost(double currentX,double currentY){

        Coordinate origin=new Coordinate(currentX,currentY);
        //FIXME: this function might return unscaled values
      return   (origin.distance(distanation)*mCost)/mDistance;

    }


}
