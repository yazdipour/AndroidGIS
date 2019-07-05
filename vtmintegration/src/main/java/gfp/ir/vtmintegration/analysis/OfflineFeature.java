package gfp.ir.vtmintegration.analysis;

import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.core.BoundingBox;

import java.util.HashMap;

import gfp.ir.vtmintegration.geojson.geojson.Geometry;
import gfp.ir.vtmintegration.geolibrary.database.GPLog;
import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;
import gfp.ir.vtmintegration.spatilite_core.util.SpatialiteUtilities;
import gfp.ir.vtmintegration.vtm.Spatialite.OfflineSearch;
import jsqlite.Constants;
import jsqlite.Exception;
import jsqlite.Stmt;

public class OfflineFeature {

    double raduis = 0.0001;

    private SpatialiteDatabaseHandler mSpatiliteDatabaseHandeler;

    public OfflineFeature(SpatialiteDatabaseHandler mSpatiliteDatabaseHandeler,
                          SpatialVectorTable table, double x, double y, BoundingBox viewport) {
        this.mSpatiliteDatabaseHandeler = mSpatiliteDatabaseHandeler;

        String q = String.format("SELECT  AsGeoJSON(geometry),id as pk,* from %s as tbl1 WHERE (\n (tbl1.ROWID IN\n  (\n   SELECT ROWID FROM SpatialIndex\n   WHERE\n   (\n    (f_table_name = '%s') AND\n    (f_geometry_column='geometry') AND\n    (search_frame = BuildMbr(%s , %s , %s, %s,4326 ))\n   )\n  )\n ) AND\n -- condition 2: (will run only on records found during condition 1)\n --> a more precise, taking into account any curves the POLYGON may contain [extensive]\n (st_intersects(geometry,BuildCircleMbr( %s ,%s, %s ,4326 ) ) == 1 )\n);", table.getTableName(), table.getTableName(), viewport.getMinLongitude(), viewport.getMinLatitude(), viewport.getMaxLongitude(), viewport.getMaxLatitude(), x, y, raduis);

        getObjectByQuery(q);
//        getObjectByQuery("select AsGeoJSON(geometry),id as pk,* from "+table.getTableName()+" where st_intersects(geometry,st_geomfromtext('Point("+x+" "+y+")'))");
    }

    public void getObjectByQuery(String q) {
        Stmt stmt;
        try {
            stmt = mSpatiliteDatabaseHandeler.getDatabase().prepare(q);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            stmt.step();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        int i = 1;
        int columnCount = 0;
        try {
            if (stmt != null) {
                columnCount = stmt.column_count();
                String geom = stmt.column_string(0);
                if (geom != null && !geom.equals(SpatialiteUtilities.DUMMY)) {
                    this.setGeojson(geom);

                }
                JSONObject geometryjsonObj = new JSONObject(getGeojson());
                JSONObject propertiesjsonObj = new JSONObject();

                for (int ii = 1; ii < columnCount; ii++) {


                    switch (stmt.column_type(ii)) {
                        case Constants.SQLITE_INTEGER:
                            propertiesjsonObj.put(stmt.column_name(ii), stmt.column_int(ii));
                            addField(stmt.column_name(ii), String.valueOf(stmt.column_int(ii)));
                            break;
                        case Constants.SQLITE_FLOAT:
                            propertiesjsonObj.put(stmt.column_name(ii), stmt.column_double(ii));
                            addField(stmt.column_name(ii), String.valueOf(stmt.column_int(ii)));
                            break;
                        case Constants.SQLITE3_TEXT:
                            propertiesjsonObj.put(stmt.column_name(ii), stmt.column_string(ii));
                            addField(stmt.column_name(ii), String.valueOf(stmt.column_int(ii)));
                            break;

                    }


                }


                feature = new JSONObject();

                feature.put("type", "Feature");
                feature.put("properties", propertiesjsonObj);
                feature.put("geometry", geometryjsonObj);
            }
        } catch (Exception e) {
            GPLog.error(this, "GeometryIterator.setLabelAndThemeText column_count[" + columnCount + "] column[" + i + "]", e);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private int id;
    private HashMap<String, String> fields = new HashMap<>();


    private void addField(String k, String v) {
        if (k.equals("pk")) {

            id = Integer.valueOf(v);
        }
        fields.put(k, v);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String geojson;

    public String getGeojson() {
        return geojson;
    }

    public void setGeojson(String geojson) {
        this.geojson = geojson;
    }


    public HashMap<String, String> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, String> fields) {
        this.fields = fields;
    }

    private JSONObject feature;

    public JSONObject getFeature() {
        return feature;
    }

    public void setFeature(JSONObject feature) {
        this.feature = feature;
    }
}
