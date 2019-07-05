package gfp.ir.vtmintegration.vtm;

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
import org.oscim.utils.ColorUtil;

import java.io.IOException;
import java.util.List;

import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.spatilite_core.core.geometry.GeometryIterator;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;
import jsqlite.Exception;

public class SpatiliteVectorLayerProvider extends VectorLayer {
    private  String tablename=null;
    private  String dbpath=null;
    private  String geomcolumn=null;
    Map map_;

    public SpatiliteVectorLayerProvider(String spatilitedbpath,String geomcolumn_,String table,Map map) {
        super(map);
        tablename=table;
        dbpath=spatilitedbpath;
        geomcolumn=geomcolumn_;
        map_=map;
        readfromdb("select ST_AsBinary(ST_Transform("+geomcolumn+",4326)),* from '"+tablename+"'");
        init();

    }
    private WKBReader wkbReader = new WKBReader();

    public SpatiliteVectorLayerProvider(Map map,List< byte[] > wkbgeoms){
        super(map);

        Style.Builder sb = Style.builder()
                .buffer(0.5)
                .fillColor(Color.RED)
                .fillAlpha(0.2f);

        for(int i=0;i<wkbgeoms.size();i++) {
            try {
                Geometry element = wkbReader.read(wkbgeoms.get(i));


                Style style = sb.buffer(Math.random() + 0.2)
                        .fillColor(ColorUtil.setHue(Color.RED,
                                (int) (Math.random() * 50) / 50.0))
                        .fillAlpha(0.5f)
                        .build();

                if( element!=null){
                    if(element.isValid()) {
                        for (int j = 0; j < element.getNumGeometries(); j++) {
                            switch (element.getGeometryType()) {
                                case "Polygon":
                                case "MultiPolygon":
                                    this.add(new PolygonDrawable(element.getGeometryN(j), style));
                                    break;
                                case "Point":
                                case "MultiPoint":
                                    this.add(new PointDrawable(new GeoPoint(element.getCoordinate().x,element.getCoordinate().y), style));

                                    break;
                                case "LineString":
                                case "MultiLineString":

                                    this.add(new LineDrawable(element.getGeometryN(j), style));
                                    break;
                            }
                        }
                    }

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void readfromdb(String query) {
        try {
            SpatialiteDatabaseHandler spd=new SpatialiteDatabaseHandler(dbpath);
//            spd.getGeometryIteratorInBounds()
            List<SpatialVectorTable> spatilaltables= spd.getSpatialVectorTables(true);

            //            SpatialVectorTable sptvt= spatilaltables.get(0);
//            sptvt.getGeomType();
//
//            int geoms=   DaoSpatialite.getGeometriesCount(spd.getDatabase(),tablename,"geom");

            Style.Builder sb = Style.builder()
                    .buffer(0.5)
                    .fillColor(Color.RED)
                    .fillAlpha(0.2f);

            GeometryIterator geoit=new GeometryIterator(spd.getDatabase(),query);
            while(geoit.hasNext()) {
                Geometry element = geoit.next();
                Style style = sb.buffer(Math.random() + 0.2)
                        .fillColor(ColorUtil.setHue(Color.RED,
                                (int) (Math.random() * 50) / 50.0))
                        .fillAlpha(0.5f)
                        .build();



                if( element!=null){
                    if(element.isValid()) {

                        this.add( element, style);

                    }

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(){

        this.update();
        map_.layers().add(this);
    }






}
