/*
 * Geopaparazzi - Digital field mapping on Android based devices
 * Copyright (C) 2010  HydroloGIS (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gfp.ir.vtmintegration.spatilite_core.core.iterator;

//import jts.geom.Geometry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import gfp.ir.vtmintegration.geolibrary.database.GPLog;
import gfp.ir.vtmintegration.spatilite_core.util.SpatialiteUtilities;
import jsqlite.Constants;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Class that iterates over Database geometries and doesn't keep everything in memory.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class SpatialiteGeojsonIterator implements Iterator<JSONObject> {
    private Stmt stmt;
    private String properties = "";
    private String geojson = "";

    /**
     * Returns Properties String (if any)
     *
     * @return the label.
     */
    public String getProperties() {
        return properties;
    }

    public String getGeojson() {
        return geojson;
    }

    /**
     * Builds Label String (if any)
     * <p>
     * Assumes that column 0 is a Geometry, 1 label, 2 theme. The keyword 'dummy' means no label/theme.
     *
     * @param stmt statement being executed
     */
    private JSONObject extractFeature(Stmt stmt) {
        properties = "";
        int i = 1;
        int columnCount = 0;
        try {
            if (stmt != null) {
                columnCount = stmt.column_count();
                String geom = stmt.column_string(0);
                if (geom!=null&&!geom.equals(SpatialiteUtilities.DUMMY)) {
                    geojson = geom;
                }
                JSONObject geometryjsonObj = new JSONObject( getGeojson());
                JSONObject propertiesjsonObj = new JSONObject();

                for (int ii=1;ii<columnCount;ii++){


                    switch (stmt.column_type(ii)){
                        case Constants.SQLITE_INTEGER:
                            propertiesjsonObj.put(stmt.column_name(ii),stmt.column_int(ii));
                            break;
                        case Constants.SQLITE_FLOAT:
                            propertiesjsonObj.put(stmt.column_name(ii),stmt.column_double(ii));
                            break;
                        case Constants.SQLITE3_TEXT:
                            propertiesjsonObj.put(stmt.column_name(ii),stmt.column_string(ii));
                            break;

                    }


                }


                JSONObject json = new JSONObject();

                json.put("type", "Feature");
                json.put("properties",propertiesjsonObj);
                json.put("geometry", geometryjsonObj);
                return json;
            }
        } catch (Exception e) {
            GPLog.error(this, "GeometryIterator.setLabelAndThemeText column_count[" + columnCount + "] column[" + i + "]", e);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Constructor.
     *
     * @param database the database to use.
     * @param query    the query to use.
     */
    public SpatialiteGeojsonIterator(Database database, String query) {
        try {
            stmt = database.prepare(query);
        } catch (Exception e) {
            GPLog.error(this, "GeometryIterator.creation sql[" + query + "]", e);
        }
    }

    @Override
    public boolean hasNext() {
        if (stmt == null) {
            return false;
        }
        try { // sqlite-amalgamation-3080100 allways returns false with BLOBS
            return stmt.step();
        } catch (Exception e) {
            GPLog.error(this, "GeometryIterator.hasNext()[stmt.step() failed]", e);
            return false;
        }
    }

    @Override
    public JSONObject next() {
        if (stmt == null) {
            GPLog.androidLog(4, "GeometryIterator.next() [stmt=null]");
            return null;
        }
        try {

           return extractFeature(stmt);


        } catch (java.lang.Exception e) {
            GPLog.error(this, "GeometryIterator.next()[wkbReader.read() failed]", e);
        }
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Reset the iterator.
     *
     * @throws Exception if something goes wrong.
     */
    public void reset() throws Exception {
        if (stmt != null)
            stmt.reset();
    }

    /**
     * Close the iterator.
     *
     * @throws Exception if something goes wrong.
     */
    public void close() throws Exception {
        if (stmt != null)
            stmt.close();
    }


    public JSONObject getAllFeatures() throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.put("type","FeatureCollection");

        while (this.hasNext()){
            JSONObject element = this.next();

            if( element!=null){
                array.put(element);
            }
        }

        result.put("features",array);

        return result;

    }
}
