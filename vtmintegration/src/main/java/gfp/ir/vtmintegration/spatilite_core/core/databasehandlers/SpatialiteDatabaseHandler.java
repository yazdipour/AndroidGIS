/*
* Geopaparazzi - Digital field mapping on Android based devices
* Copyright (C) 2010 HydroloGIS (www.hydrologis.com)
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package gfp.ir.vtmintegration.spatilite_core.core.databasehandlers;

import android.util.Log;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gfp.ir.vtmintegration.geolibrary.database.GPLog;
import gfp.ir.vtmintegration.geolibrary.style.Style;
import gfp.ir.vtmintegration.geolibrary.util.LibraryConstants;
import gfp.ir.vtmintegration.geolibrary.util.types.ESpatialDataSources;
import gfp.ir.vtmintegration.spatilite_core.core.daos.DaoSpatialite;
import gfp.ir.vtmintegration.spatilite_core.core.daos.DatabaseCreationAndProperties;
import gfp.ir.vtmintegration.spatilite_core.core.daos.GeopaparazziDatabaseProperties;
import gfp.ir.vtmintegration.spatilite_core.core.daos.ISpatialiteTableAndFieldsNames;
import gfp.ir.vtmintegration.spatilite_core.core.enums.GeometryType;
import gfp.ir.vtmintegration.spatilite_core.core.enums.SpatialiteDatabaseType;
import gfp.ir.vtmintegration.spatilite_core.core.enums.TableTypes;
import gfp.ir.vtmintegration.spatilite_core.core.geometry.GeometryBufferIterator;
import gfp.ir.vtmintegration.spatilite_core.core.geometry.GeometryIterator;
import gfp.ir.vtmintegration.spatilite_core.core.tables.AbstractSpatialTable;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialRasterTable;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;
import gfp.ir.vtmintegration.spatilite_core.util.SpatialiteUtilities;
import gfp.ir.vtmintegration.spatilite_core.util.comparators.OrderComparator;
import jsqlite.Callback;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

import static gfp.ir.vtmintegration.spatilite_core.core.daos.GeopaparazziDatabaseProperties.createDefaultPropertiesForTable;
import static gfp.ir.vtmintegration.spatilite_core.core.daos.GeopaparazziDatabaseProperties.createPropertiesTable;
import static gfp.ir.vtmintegration.spatilite_core.core.daos.GeopaparazziDatabaseProperties.deleteStyleTable;
import static gfp.ir.vtmintegration.spatilite_core.core.daos.GeopaparazziDatabaseProperties.getAllStyles;
import static gfp.ir.vtmintegration.spatilite_core.core.daos.GeopaparazziDatabaseProperties.getStyle4Table;
import static gfp.ir.vtmintegration.spatilite_core.core.daos.GeopaparazziDatabaseProperties.updateStyleName;
/**
 * An utility class to handle the spatial database.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class SpatialiteDatabaseHandler extends AbstractSpatialDatabaseHandler {

    private String uniqueDbName4DataProperties = "";

    private Database dbJava;
    private List<SpatialVectorTable> vectorTableList;
    private List<SpatialRasterTable> rasterTableList;

    private SpatialiteDatabaseType databaseType = null;

    // List of all SpatialView of Database [view_name,view_data] - parse for
    // 'geometry_column;min_x,min_y,max_x,max_y'
    private HashMap<String, String> spatialVectorMap = new HashMap<String, String>();
    // List of all SpatialView of Database [view_name,view_data] - that have errors
    private HashMap<String, String> spatialVectorMapErrors = new HashMap<String, String>();

    private volatile boolean isOpen = false;

    /**
     * Constructor.
     *
     * @param dbPath the path to the database this handler connects to.
     * @throws IOException if something goes wrong.
     */
    public SpatialiteDatabaseHandler(String dbPath) throws IOException {
        super(dbPath);
        open();
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void open() {
        try {
            uniqueDbName4DataProperties = databasePath;
            dbJava = new jsqlite.Database();
            try {
                dbJava.open(databasePath, jsqlite.Constants.SQLITE_OPEN_READWRITE | jsqlite.Constants.SQLITE_OPEN_CREATE);
                isOpen = true;
                isDatabaseValid = true;
            } catch (Exception e) {
                GPLog.error(this, "Database marked as invalid: " + databasePath, e);
                isDatabaseValid = false;
                isOpen = false;
                GPLog.androidLog(4, "SpatialiteDatabaseHandler[" + databaseFile.getAbsolutePath() + "].open has failed", e);
            }
            if (isValid()) {
                // check database and collect the views list
                try {
                    databaseType = DatabaseCreationAndProperties.checkDatabaseTypeAndValidity(dbJava, spatialVectorMap, spatialVectorMapErrors);
                } catch (Exception e) {
                    GPLog.error(this, null, e);
                    isDatabaseValid = false;
                    isOpen = false;
                }
                switch (databaseType) {
             /*
               if (spatialVectorMap.size() == 0) for SPATIALITE3/4
                --> DaoSpatialite.checkDatabaseTypeAndValidity will return SpatialiteDatabaseType.UNKNOWN
                -- there is nothing to load (database empty)
             */
                    case GEOPACKAGE:
                    case SPATIALITE3:
                    case SPATIALITE4:
                        isDatabaseValid = true;
                        break;
                    default: {
                        isDatabaseValid = false;
                        isOpen = false;
                    }
                }
            }
            if (!isValid()) {
                close();
            } else { // avoid call for invalid databases [SpatialiteDatabaseType.UNKNOWN]
                checkAndUpdatePropertiesUniqueNames();
            }
        } catch (Exception e) {
            GPLog.error(this, "SpatialiteDatabaseHandler[" + databaseFile.getAbsolutePath() + "]", e);
        }
    }

    /**
     * Is the database file considered valid?
     * <p/>
     * <br>- metadata table exists and has data
     * <br>- 'tiles' is either a table or a view and the correct fields exist
     * <br>-- if a view: do the tables map and images exist with the correct fields
     * <br>checking is done once when the 'metadata' is retrieved the first time [fetchMetadata()]
     *
     * @return true if valid, otherwise false
     */
    @Override
    public boolean isValid() {
        return isDatabaseValid;
    }

    @Override
    public List<SpatialVectorTable> getSpatialVectorTables(boolean forceRead) throws Exception {
        if (vectorTableList == null || forceRead) {
            vectorTableList = new ArrayList<SpatialVectorTable>();
            checkAndCollectTables();
        }
        return vectorTableList;
    }

    @Override
    public List<SpatialRasterTable> getSpatialRasterTables(boolean forceRead) throws Exception {
        if (rasterTableList == null || forceRead) {
            rasterTableList = new ArrayList<SpatialRasterTable>();
            checkAndCollectTables();
        }
        return rasterTableList;
    }

    /**
     * Checks if the table names in the properties table are defined properly.
     * <p/>
     * <p>The unique table name is a concatenation of:<br>
     * <b>dbPath#tablename#geometrytype</b>
     * <p>If the name doesn't start with the database path, it needs to
     * be updated. The rest is anyways unique inside the database.
     *
     * @throws Exception if something went wrong.
     */
    private void checkAndUpdatePropertiesUniqueNames() throws Exception {
        List<Style> allStyles = null;
        try {
            allStyles = getAllStyles(dbJava);
        } catch (java.lang.Exception e) {
            // ignore and create a default one
        }
        if (allStyles == null) {
            /*
            * something went wrong in the reading of the table,
            * which might be due to an upgrade of table structure.
            * Remove and recreate the table.
            */
            deleteStyleTable(dbJava);
            createPropertiesTable(dbJava);
        } else {
            for (Style style : allStyles) {
                if (!style.name.startsWith(SpatialVectorTable.TABLENAMEPRE + SpatialiteUtilities.UNIQUENAME_SEPARATOR)) {
                    // need to update the name in the style and also in the database
                    String[] split = style.name.split(SpatialiteUtilities.UNIQUENAME_SEPARATOR);
                    if (split.length == 3) {
                        String newName = SpatialVectorTable.TABLENAMEPRE + SpatialiteUtilities.UNIQUENAME_SEPARATOR + split[1]
                                + SpatialiteUtilities.UNIQUENAME_SEPARATOR + split[2];
                        style.name = newName;
                        updateStyleName(dbJava, newName, style.id);
                    }
                }
            }
        }
    }

    /**
     * Check availability of style for the tables.
     *
     * @throws Exception
     */
    private void checkPropertiesTable() throws Exception {
        int propertiesTableColumnCount = DatabaseCreationAndProperties.checkTableExistence(dbJava, ISpatialiteTableAndFieldsNames.PROPERTIESTABLE);
        if (propertiesTableColumnCount == 0) {
            createPropertiesTable(dbJava);
            for (SpatialVectorTable spatialTable : vectorTableList) {
                createDefaultPropertiesForTable(dbJava, spatialTable.getUniqueNameBasedOnTableName(),
                        spatialTable.getLabelField());
            }
        }
    }

    public float[] getTableBounds(AbstractSpatialTable spatialTable) throws Exception {
        return spatialTable.getTableBounds();
    }

    /**
     * Retrieve list of WKB geometries from the given table in the given bounds.
     *
     * @param destSrid the destination srid.
     * @param table    the vector table.
     * @param n        north bound.
     * @param s        south bound.
     * @param e        east bound.
     * @param w        west bound.
     * @return list of WKB geometries.
     */
    public List<byte[]> getWKBFromTableInBounds(String destSrid, SpatialVectorTable table, double n, double s, double e, double w) {
        List<byte[]> list = new ArrayList<byte[]>();
        String query = SpatialiteUtilities.buildGeometriesInBoundsQuery(destSrid, false, table, n, s, e, w);
        try {
            Stmt stmt = dbJava.prepare(query);
            try {
                while (stmt.step()) {
                    list.add(stmt.column_bytes(0));
                }
            } finally {
                stmt.close();
            }
            return list;
        } catch (Exception ex) {
            GPLog.error(this, null, ex);
        }
        return null;
    }

    @Override
    public byte[] getRasterTile(String query) {
        try {
            Stmt stmt = dbJava.prepare(query);
            try {
                if (stmt.step()) {
                    return stmt.column_bytes(0);
                }
            } finally {
                stmt.close();
            }
        } catch (Exception ex) {
            GPLog.error(this, null, ex);
        }
        return null;
    }








    /**
     * Get the {@link GeometryIterator} of a table in a given bound.
     *
     * @param destSrid the srid to which to transform to.
     * @param table    the table to use.
     * @param n        north bound.
     * @param s        south bound.
     * @param e        east bound.
     * @param w        west bound.
     * @return the geometries iterator.
     */
    public GeometryIterator getGeometryIteratorInBounds(String destSrid, SpatialVectorTable table, double n, double s, double e,
                                                        double w) {

        String query = SpatialiteUtilities.buildGeometriesInBoundsQuery(destSrid, false, table, n, s, e, w);
        // GPLog.androidLog(-1,"GeopaparazziOverlay.getGeometryIteratorInBounds query["+query+"]");
        return new GeometryIterator(dbJava, query);
    }


    /**
     * Get the {@link GeometryIterator} of a table in a given bound.
     *
     * @param destSrid the srid to which to transform to.
     * @param table    the table to use.
     * @param n        north bound.
     * @param s        south bound.
     * @param e        east bound.
     * @param w        west bound.
     * @return the geometries iterator.
     */
    public GeometryIterator getGeometryIteratorInBounds(String destSrid, SpatialVectorTable table, double n, double s, double e,
                                                        double w,String boundsrid ) {
        String query = SpatialiteUtilities.buildGeometriesInBoundsQuery(destSrid, false, table, n, s, e, w,boundsrid);
        // GPLog.androidLog(-1,"GeopaparazziOverlay.getGeometryIteratorInBounds query["+query+"]");
        return new GeometryIterator(dbJava, query);
    }





    /**
     * Get the {@link GeometryIterator} of a table in a given bound.
     *
     * @param destSrid the srid to which to transform to.
     * @param table    the table to use.
     * @param n        north bound.
     * @param s        south bound.
     * @param e        east bound.
     * @param w        west bound.
     * @return the geometries iterator.
     */
    public GeometryBufferIterator getGeometryBufferIteratorInBounds(String destSrid, SpatialVectorTable table, double n, double s, double e,
                                                              double w, String boundsrid ) {
        String query = SpatialiteUtilities.buildGEoJsonGeometriesInBoundsQuery(destSrid, false, table, n, s, e, w,boundsrid);
        // GPLog.androidLog(-1,"GeopaparazziOverlay.getGeometryIteratorInBounds query["+query+"]");
        return new GeometryBufferIterator(dbJava, query);
    }


    public void close() throws Exception {
        if (isOpen) {
            isOpen = false;
            if (dbJava != null) {
                dbJava.close();
            }
        }
    }

    /**
     * Performs an intersection query on a vector table and returns a string info version of the result.
     *
     * @param boundsSrid          the srid of the bounds supplied.
     * @param spatialTable        the vector table to query.
     * @param n                   north bound.
     * @param s                   south bound.
     * @param e                   east bound.
     * @param w                   west bound.
     * @param resultStringBuilder the builder of the result.
     * @param indentStr           the indenting to use for formatting.
     * @throws Exception if something goes wrong.
     */
    public void intersectionToStringBBOX(String boundsSrid, SpatialVectorTable spatialTable, double n, double s, double e,
                                         double w, StringBuilder resultStringBuilder, String indentStr) throws Exception {
        String query = getIntersectionQueryBBOX(boundsSrid, spatialTable, n, s, e, w);
        Stmt stmt = dbJava.prepare(query);
        try {
            while (stmt.step()) {
                int column_count = stmt.column_count();
                for (int i = 0; i < column_count; i++) {
                    String cName = stmt.column_name(i);
                    String value = stmt.column_string(i);
                    resultStringBuilder.append(indentStr).append(cName).append(": ").append(value).append("\n");
                }
                resultStringBuilder.append("\n");
            }
        } finally {
            stmt.close();
        }
    }

    /**
     * Get the query to run for a bounding box intersection.
     * <p/>
     * <p>This assures that the first element of the query is
     * the id field for the record as defined in {@link SpatialiteUtilities#SPATIALTABLE_ID_FIELD}.
     *
     * @param boundsSrid   the srid of the bounds requested.
     * @param spatialTable the {@link SpatialVectorTable} to query.
     * @param n            north bound.
     * @param s            south bound.
     * @param e            east bound.
     * @param w            west bound.
     * @return the query to run to get all fields.
     */
    public static String getIntersectionQueryBBOX(String boundsSrid, SpatialVectorTable spatialTable, double n, double s,
                                                  double e, double w) {
        boolean doTransform = false;
        String fieldNamesList = SpatialiteUtilities.SPATIALTABLE_ID_FIELD;
        // List of non-blob fields
        for (String field : spatialTable.getTableFieldNamesList()) {
            boolean ignore = SpatialiteUtilities.doIgnoreField(field);
            if (!ignore)
                fieldNamesList += "," + field;
        }
        if (!spatialTable.getSrid().equals(boundsSrid)) {
            doTransform = true;
        }
        StringBuilder sbQ = new StringBuilder();
        sbQ.append("SELECT ");
        sbQ.append(fieldNamesList);
        sbQ.append(" FROM \"").append(spatialTable.getTableName());
        sbQ.append("\" WHERE ST_Intersects(");
        if (doTransform)
            sbQ.append("ST_Transform(");
        sbQ.append("BuildMBR(");
        sbQ.append(w);
        sbQ.append(",");
        sbQ.append(s);
        sbQ.append(",");
        sbQ.append(e);
        sbQ.append(",");
        sbQ.append(n);
        if (doTransform) {
            sbQ.append(",");
            sbQ.append(boundsSrid);
            sbQ.append("),");
            sbQ.append(spatialTable.getSrid());
        }
        sbQ.append("),");
        sbQ.append(spatialTable.getGeomName());
        sbQ.append(");");

        return sbQ.toString();
    }

    // public void intersectionToString4Polygon( String queryPointSrid, SpatialVectorTable
    // spatialTable, double n, double e,
    // StringBuilder sb, String indentStr ) throws Exception {
    // boolean doTransform = false;
    // if (!spatialTable.getSrid().equals(queryPointSrid)) {
    // doTransform = true;
    // }
    //
    // StringBuilder sbQ = new StringBuilder();
    // sbQ.append("SELECT * FROM ");
    // sbQ.append(spatialTable.getName());
    // sbQ.append(" WHERE ST_Intersects(");
    // sbQ.append(spatialTable.getGeomName());
    // sbQ.append(",");
    // if (doTransform)
    // sbQ.append("ST_Transform(");
    // sbQ.append("MakePoint(");
    // sbQ.append(e);
    // sbQ.append(",");
    // sbQ.append(n);
    // if (doTransform) {
    // sbQ.append(",");
    // sbQ.append(queryPointSrid);
    // sbQ.append("),");
    // sbQ.append(spatialTable.getSrid());
    // }
    // sbQ.append(")) = 1 ");
    // sbQ.append("AND ROWID IN (");
    // sbQ.append("SELECT ROWID FROM Spatialindex WHERE f_table_name ='");
    // sbQ.append(spatialTable.getName());
    // sbQ.append("'");
    // // if a table has more than 1 geometry, the column-name MUST be given, otherwise no results.
    // sbQ.append(" AND f_geometry_column = '");
    // sbQ.append(spatialTable.getGeomName());
    // sbQ.append("'");
    // sbQ.append(" AND search_frame = ");
    // if (doTransform)
    // sbQ.append("ST_Transform(");
    // sbQ.append("MakePoint(");
    // sbQ.append(e);
    // sbQ.append(",");
    // sbQ.append(n);
    // if (doTransform) {
    // sbQ.append(",");
    // sbQ.append(queryPointSrid);
    // sbQ.append("),");
    // sbQ.append(spatialTable.getSrid());
    // }
    // sbQ.append("));");
    // String query = sbQ.toString();
    //
    // Stmt stmt = db_java.prepare(query);
    // try {
    // while( stmt.step() ) {
    // int column_count = stmt.column_count();
    // for( int i = 0; i < column_count; i++ ) {
    // String cName = stmt.column_name(i);
    // if (cName.equalsIgnoreCase(spatialTable.getGeomName())) {
    // continue;
    // }
    //
    // String value = stmt.column_string(i);
    // sb.append(indentStr).append(cName).append(": ").append(value).append("\n");
    // }
    // sb.append("\n");
    // }
    // } finally {
    // stmt.close();
    // }
    // }

    /**
     * Load list of Table [Vector/Raster] for GeoPackage Files [gpkg]
     * <p/>
     * <b>THIS METHOD IS VERY EXPERIMENTAL AND A WORK IN PROGRESS</b>
     * - rasterTableList or vectorTableList will be created if == null
     * <br>- name of Field
     * <br> - type of field as defined in Database
     * <br>- OGC 12-128r9 from 2013-11-19
     * <br>-- older versions will not be supported
     * <br>- With SQLite versions 3.7.17 and later : 'PRAGMA application_id' [1196437808]
     * <br>-- older (for us invalid) SPL_Geopackage Files return 0
     */
    private void collectGpkgTables() throws Exception {
        String vector_key = ""; // term used when building the sql, used as map.key
        String vector_value = ""; // to retrieve map.value (=vector_data+vector_extent)
        for (Map.Entry<String, String> vector_entry : spatialVectorMap.entrySet()) {
            // berlin_stadtteile
            vector_key = vector_entry.getKey();
            // soldner_polygon;14;3;2;3068;1;20847.6171111586,18733.613614603,20847.6171111586,18733.613614603
            vector_value = vector_entry.getValue();
            double[] boundsCoordinates = new double[]{0.0, 0.0, 0.0, 0.0};
            double[] centerCoordinate = new double[]{0.0, 0.0};
            HashMap<String, String> fields_list = new HashMap<String, String>();
            int i_geometry_type = 0;
            int i_view_read_only = 0;
            double horz_resolution = 0.0;
            String s_view_read_only = "";
            String[] sa_string = vector_key.split(";");
            // fromosm_tiles;tile_data;GeoPackage_tiles;© OpenStreetMap contributors, See
            // http://www.openstreetmap.org/copyright;OSM Tiles;
            // geonames;geometry;GeoPackage_features;Data from http://www.geonames.org/, under
            // Creative Commons Attribution 3.0 License;Geonames;
            if (sa_string.length == 5) {
                String table_name = sa_string[0]; // fromosm_tiles / geonames
                String geometry_column = sa_string[1]; // tile_data / geometry
                String layerType = sa_string[2]; // GeoPackage_tiles / GeoPackage_features
                String s_identifier = sa_string[3]; // short description
                String s_description = sa_string[4]; // long description
                sa_string = vector_value.split(";");
                // RGB;512;3068;1890 -
                // 1:17777;3;17903.0354299312,17211.5335278146,29889.8601630003,26582.2086184726;2014-05-09T09:18:07.230Z
                if (sa_string.length == 7) {
                    // 0;10;3857;0;
                    // 1;2;4326;0;
                    String s_geometry_type = sa_string[0]; // 1= POINT / OR min_zoom
                    String s_coord_dimension = sa_string[1]; // 2= XY / OR max_zoom
                    String s_srid = sa_string[2]; // 4326
                    String s_spatial_index_enabled = sa_string[3]; // 0
                    // -1;-75.5;18.0;-71.06667;20.08333;2013-12-24T16:32:14.000000Z
                    String s_row_count = sa_string[4]; // 0 = not possible as sub-query - but also
                    // not needed
                    String s_bounds = sa_string[5]; // -75.5;18.0;-71.06667;20.08333
                    String s_last_verified = sa_string[6]; // 2013-12-24T16:32:14.000000Z
                    sa_string = s_bounds.split(",");
                    if (sa_string.length == 4) {
                        try {
                            boundsCoordinates[0] = Double.parseDouble(sa_string[0]);
                            boundsCoordinates[1] = Double.parseDouble(sa_string[1]);
                            boundsCoordinates[2] = Double.parseDouble(sa_string[2]);
                            boundsCoordinates[3] = Double.parseDouble(sa_string[3]);
                        } catch (NumberFormatException e) {
                        }
                        if (!s_srid.equals("4326")) { // Transform into wsg84 if needed
                            SpatialiteUtilities.collectBoundsAndCenter(dbJava, s_srid, centerCoordinate, boundsCoordinates);
                        } else {
                            centerCoordinate[0] = boundsCoordinates[0] + (boundsCoordinates[2] - boundsCoordinates[0]) / 2;
                            centerCoordinate[1] = boundsCoordinates[1] + (boundsCoordinates[3] - boundsCoordinates[1]) / 2;
                        }
                        checkAndAdaptDatabaseBounds(boundsCoordinates, null);
                        if (vector_key.contains("GeoPackage_tiles")) {
                            int i_min_zoom = Integer.parseInt(s_geometry_type);
                            int i_max_zoom = Integer.parseInt(s_coord_dimension);
                            SpatialRasterTable table = new SpatialRasterTable(getDatabasePath(), "", s_srid, i_min_zoom,
                                    i_max_zoom, centerCoordinate[0], centerCoordinate[1], null, boundsCoordinates);
                            table.setMapType(layerType);
                            // table.setTableName(s_table_name);
                            table.setColumnName(geometry_column);
                            // setDescription(s_table_name);
                            // table.setDescription(this.databaseDescription);
                            if (rasterTableList == null)
                                rasterTableList = new ArrayList<SpatialRasterTable>();
                            rasterTableList.add(table);
                        } else {
                            if (vector_key.contains("GeoPackage_features")) {
                                // String table_name=sa_string[0]; // lakemead_clipped
                                // String geometry_column=sa_string[1]; // shape
                                i_view_read_only = 0; // always
                                i_geometry_type = Integer.parseInt(s_geometry_type);
                                GeometryType geometry_type = GeometryType.forValue(i_geometry_type);
                                s_geometry_type = geometry_type.toString();
                                int i_spatial_index_enabled = Integer.parseInt(s_spatial_index_enabled); // 0=no
                                // spatialiIndex
                                // for
                                // GeoPackage
                                // Files
                                int i_row_count = Integer.parseInt(s_row_count); // will always be 0
                                // no Zoom levels with
                                // vector data
                                if (i_spatial_index_enabled == 1) {
                                    SpatialVectorTable table = new SpatialVectorTable(getDatabasePath(), table_name,
                                            geometry_column, i_geometry_type, s_srid, centerCoordinate, boundsCoordinates,
                                            layerType);
                                    // compleate list of fields of
                                    // this table
                                    fields_list = DaoSpatialite.collectTableFields(dbJava, table_name);
                                    table.setFieldsList(fields_list, "ROWID", i_view_read_only);
                                    if (vectorTableList == null)
                                        vectorTableList = new ArrayList<SpatialVectorTable>();
                                    vectorTableList.add(table);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Load list of Table [Vector] for Spatialite4+ Files
     * - for Spaltialite4+ all needed information has been collected in DaoSpatialite.checkDatabaseTypeAndValidity()
     * - rasterTableList or vectorTableList will be created if == null
     * <br>- name of Field
     * <br>- type of field as defined in Database
     */
    private void collectVectorTables() throws Exception {
        String vector_key = ""; // term used when building the sql, used as map.key
        String vector_value = ""; // to retrieve map.value (=vector_data+vector_extent)
        for (Map.Entry<String, String> vector_entry : spatialVectorMap.entrySet()) {
            // berlin_stadtteile
            vector_key = vector_entry.getKey();
            // soldner_polygon;14;3;2;3068;1;20847.6171111586,18733.613614603,20847.6171111586,18733.613614603
            vector_value = vector_entry.getValue();
            double[] boundsCoordinates = new double[]{0.0, 0.0, 0.0, 0.0};
            double[] centerCoordinate = new double[]{0.0, 0.0};
            HashMap<String, String> fields_list = new HashMap<String, String>();
            int i_geometry_type = 0;
            int i_view_read_only = 0;
            String s_view_read_only = "";
            String[] sa_string = vector_key.split(";");
            // berlin_postgrenzen.1890;LOSSY_WEBP;RasterLite2;Berlin Straube Postgrenzen;1890 -
            // 1:17777;
            if (sa_string.length == 5) {
                String table_name = sa_string[0];
                String geometry_column = sa_string[1];
                String layerType = sa_string[2];
                String s_ROWID_PK = sa_string[3];
                s_view_read_only = sa_string[4];
                sa_string = vector_value.split(";");
                // RGB;512;3068;1.13008623862252;3;17903.0354299312,17211.5335278146,29889.8601630003,26582.2086184726;2014-05-09T09:18:07.230Z
                if (sa_string.length == 7) {
                    String s_geometry_type = sa_string[0];
                    String s_coord_dimension = sa_string[1];
                    String s_srid = sa_string[2];
                    String s_spatial_index_enabled = sa_string[3];
                    String s_row_count_enabled = sa_string[4];
                    String s_bounds = sa_string[5];
                    String s_last_verified = sa_string[6];
                    sa_string = s_bounds.split(",");
                    // must be > 0 for Wsg84 support
                    int i_srid = Integer.parseInt(s_srid);
                    if ((sa_string.length == 4) && (i_srid > 0)) {
                        try {
                            boundsCoordinates[0] = Double.parseDouble(sa_string[0]);
                            boundsCoordinates[1] = Double.parseDouble(sa_string[1]);
                            boundsCoordinates[2] = Double.parseDouble(sa_string[2]);
                            boundsCoordinates[3] = Double.parseDouble(sa_string[3]);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                        if (!s_srid.equals(LibraryConstants.SRID_WGS84_4326)) { // Transform into wsg84 if needed
                            SpatialiteUtilities.collectBoundsAndCenter(dbJava, s_srid, centerCoordinate, boundsCoordinates);
                        } else {
                            centerCoordinate[0] = boundsCoordinates[0] + (boundsCoordinates[2] - boundsCoordinates[0]) / 2;
                            centerCoordinate[1] = boundsCoordinates[1] + (boundsCoordinates[3] - boundsCoordinates[1]) / 2;
                        }
                        checkAndAdaptDatabaseBounds(boundsCoordinates, null);
                        if (layerType.equals(ESpatialDataSources.RASTERLITE2.getTypeName())) {
                            // s_ROWID_PK == title [Berlin Straube Postgrenzen] - needed
                            // s_view_read_only == abstract [1890 - 1:17777] - needed
                            // s_geometry_type == pixel_type [RGB] - not needed
                            // s_coord_dimension == tile_width - maybe usefull
                            // geometry_column == compression [LOSSY_WEBP] - not needed
                            // s_row_count_enabled == num_bands [3] - not needed
                            //                            int i_tile_width = Integer.parseInt(s_coord_dimension);
                            //                            double horz_resolution = Double.parseDouble(s_spatial_index_enabled);
                            //                            int i_num_bands = Integer.parseInt(s_row_count_enabled);
                            // TODO in next version add RasterTable
                            // berlin_postgrenzen.1890
                            SpatialRasterTable table = new SpatialRasterTable(getDatabasePath(), table_name, s_srid, 0, 22,
                                    centerCoordinate[0], centerCoordinate[1], null, boundsCoordinates);
                            table.setMapType(layerType);
                            table.setTitle(s_ROWID_PK);
                            table.setDescription(s_view_read_only);
                            // prevent a possible double loading
                            if (rasterTableList == null)
                                rasterTableList = new ArrayList<SpatialRasterTable>();
                            rasterTableList.add(table);
                        }
                        if ((layerType.equals(TableTypes.SPATIALTABLE.getDescription())) || (layerType.equals(TableTypes.SPATIALVIEW.getDescription()))) {
                            i_view_read_only = Integer.parseInt(s_view_read_only);
                            i_geometry_type = Integer.parseInt(s_geometry_type);
                            GeometryType geometry_type = GeometryType.forValue(i_geometry_type);
                            s_geometry_type = geometry_type.toString();
                            int i_spatial_index_enabled = Integer.parseInt(s_spatial_index_enabled); // should
                            // always
                            // be
                            // 1
                            int i_row_count = Integer.parseInt(s_row_count_enabled);
                            // no Zoom levels with
                            // vector data
                            if (i_spatial_index_enabled == 1) {
                                SpatialVectorTable table = new SpatialVectorTable(getDatabasePath(), table_name, geometry_column,
                                        i_geometry_type, s_srid, centerCoordinate, boundsCoordinates, layerType);
                                // compleate list of fields of
                                // this table
                                fields_list = DaoSpatialite.collectTableFields(dbJava, table_name);
                                table.setFieldsList(fields_list, s_ROWID_PK, i_view_read_only);
                                if (vectorTableList == null)
                                    vectorTableList = new ArrayList<SpatialVectorTable>();
                                vectorTableList.add(table);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks (and adapts) the overall database bounds based on the passed coordinates.
     * <p/>
     * <p>Goal: when painting the Geometries: check of viewport is inside these bounds.
     * <br>- if the Viewport is outside these Bounds: all Tables can be ignored
     * <br>-- this is called when the Tables are created
     *
     * @param boundsCoordinates bounds to check against the overall.
     */
    private void checkAndAdaptDatabaseBounds(double[] boundsCoordinates, int[] zoomLevels) {
        if ((this.boundsWest == 0.0) && (this.boundsSouth == 0.0) && (this.boundsEast == 0.0) && (this.boundsNorth == 0.0)) {
            this.boundsWest = boundsCoordinates[0];
            this.boundsSouth = boundsCoordinates[1];
            this.boundsEast = boundsCoordinates[2];
            this.boundsNorth = boundsCoordinates[2];
        } else {
            if (boundsCoordinates[0] < this.boundsWest)
                this.boundsWest = boundsCoordinates[0];
            if (boundsCoordinates[1] < this.boundsSouth)
                this.boundsSouth = boundsCoordinates[1];
            if (boundsCoordinates[2] > this.boundsEast)
                this.boundsEast = boundsCoordinates[2];
            if (boundsCoordinates[3] < this.boundsNorth)
                this.boundsNorth = boundsCoordinates[3];
        }
        centerX = this.boundsWest + (this.boundsEast - this.boundsWest) / 2;
        centerY = this.boundsSouth + (this.boundsNorth - this.boundsSouth) / 2;
        if ((zoomLevels != null) && (zoomLevels.length == 2)) {
            if ((this.minZoom == 0) && (this.maxZoom == 0)) {
                this.minZoom = zoomLevels[0];
                this.maxZoom = zoomLevels[1];
            } else {
                if (zoomLevels[0] < this.minZoom)
                    this.minZoom = zoomLevels[0];
                if (zoomLevels[1] > this.maxZoom)
                    this.maxZoom = zoomLevels[1];
            }
        }
    }

    /**
     * Collects tables.
     * <p/>
     * <p>The {@link HashMap} will contain:
     * <ul>
     * <li>name of Field
     * <li>type of field as defined in Database
     * </ul>
     */
    private void checkAndCollectTables() throws Exception {
        switch (databaseType) {
            case GEOPACKAGE: {
                // GeoPackage Files [gpkg]
                collectGpkgTables();
            }
            break;
            case SPATIALITE3:
            case SPATIALITE4: {
                // Spatialite Files version 2.4 ; 3 and 4
                collectVectorTables();
            }
            break;
            default:
                break;
        }
        if (isValid()) {
            if (vectorTableList != null) {
                // now read styles
                checkPropertiesTable();
                // assign the styles
                for (SpatialVectorTable spatialTable : vectorTableList) {
                    Style style4Table = null;

                    File parentFile = spatialTable.getDatabaseFile().getParentFile();
                    boolean canWrite = parentFile.canWrite();
                    try {
                        style4Table = getStyle4Table(dbJava, spatialTable.getUniqueNameBasedOnTableName(),
                                spatialTable.getLabelField());
                    } catch (java.lang.Exception e) {
                        if (canWrite) {
                            deleteStyleTable(dbJava);
                            checkPropertiesTable();
                        }
                    }
                    if (style4Table == null) {
                        spatialTable.makeDefaultStyle();
                    } else {
                        spatialTable.setStyle(style4Table);
                    }
                }
                OrderComparator orderComparator = new OrderComparator();
                Collections.sort(vectorTableList, orderComparator);
            }
        }
    }

    /**
     * Update a style definiton in the db.
     *
     * @param style the {@link Style} to update.
     * @throws Exception if something goes wrong.
     */
    public void updateStyle(Style style) throws Exception {
        GeopaparazziDatabaseProperties.updateStyle(dbJava, style);
    }

    /**
     * Delete and recreate a default properties table for this database.
     *
     * @throws Exception if something goes wrong.
     */
    public void resetStyleTable() throws Exception {
        deleteStyleTable(dbJava);
        createPropertiesTable(dbJava);
        for (SpatialVectorTable spatialTable : vectorTableList) {
            createDefaultPropertiesForTable(dbJava, spatialTable.getUniqueNameBasedOnTableName(),
                    spatialTable.getLabelField());
        }
    }

    /**
     * Getter for the spatialite db reference.
     *
     * @return the spatialite database reference.
     */
    public Database getDatabase() {
        return dbJava;
    }

    /**
     * This function just returns the list of String values which are distinct
     * @param spatialTable
     * @param Field
     * @return
     */

   public List<String> getdistinctfield(SpatialVectorTable spatialTable,String Field){

        boolean fieldexist=spatialTable.getTableFieldNamesList().contains(Field);
        List<String> list = new ArrayList<>();

        if(fieldexist){
        String query = SpatialiteUtilities.buildFieldDistinctValues(spatialTable.getTableName(), Field);
        try {

            jsqlite.Stmt stmt = dbJava.prepare(query);
            try {
//                int columntype= stmt.column_type(0);
//                if(columntype>3 && columntype<1){
//
//                    if(columntype==3){
//                        spatialTable.setStyleFieldType(EDataType.TEXT);
//                        }
//                        else if (columntype==2)
//                        {
//                        spatialTable.setStyleFieldType(EDataType.FLOAT);
//
//                        }
//                        else
//                            {
//                        spatialTable.setStyleFieldType(EDataType.INTEGER);
//                            }

                while (stmt.step()) {
                    list.add(stmt.column_string(0));
//                switch(columntype){
//                    case 3:
//                        list.add(stmt.column_string(0));
//                        break;
//                    case 2:
////                        list.add(stmt.column_double(0));
//                        break;
//                     case 1:
////                        list.add(stmt.column_long(0));
//                        break;
//
//                    }

                }

//                }
//


            } finally {
                stmt.close();
            }


            spatialTable.setdistinctfield(list);
            spatialTable.getStyle().themeField=Field;


            return list;
        } catch (java.lang.Exception ex) {
            GPLog.error(this, null, ex);
        }
        return null;

        }
        return null;

        }


    public boolean buildCreateRoutingNodes(SpatialVectorTable spatialTable){





            String query = SpatialiteUtilities.buildCreateRoutingNodes(spatialTable.getTableName(), "node_from",
                    "node_to",spatialTable.getGeomName());
            try {


                dbJava.exec(query, new Callback() {
                    @Override
                    public void columns(String[] coldata) {

                    }

                    @Override
                    public void types(String[] types) {

                    }

                    @Override
                    public boolean newrow(String[] rowdata) {
                        return false;
                    }
                });




                return true;
            } catch (java.lang.Exception ex) {
                GPLog.error(this, ex.getMessage(), ex);
                return false;

            }

    }


    public boolean buildVitrualRouting(SpatialVectorTable spatialTable){




        String query = SpatialiteUtilities.buildCreateRouting(spatialTable.getTableName(), "node_from",
                "node_to",spatialTable.getGeomName());
        try {


            dbJava.exec(query, new Callback() {
                @Override
                public void columns(String[] coldata) {

                }

                @Override
                public void types(String[] types) {

                }

                @Override
                public boolean newrow(String[] rowdata) {
                    return false;
                }
            });




            return true;
        } catch (java.lang.Exception ex) {
            GPLog.error(this, ex.getMessage(), ex);
            return false;

        }

    }



    public boolean RoutingTableExists(String tablename) {


        String query = SpatialiteUtilities.buildTableExists(tablename, "table", "%VirtualRouting(%");

            try {

                jsqlite.Stmt stmt = dbJava.prepare(query);
                int count = 0;
                try {

                    if (stmt.step()) {

                        count = stmt.column_int(0);

                    }


                } finally {
                    stmt.close();
                }


                if (count > 0)
                    return true;
                else
                    return false;

            } catch (java.lang.Exception ex) {
                GPLog.error(this, ex.getMessage(), ex);
                return false;

            }


    }

//    /**
//     * Get Id of nearest point to a lat lon
//     * @param spatialTable table
//     * @param lon our point
//     * @param lat our point
//     * @param limit limit numbers
//     * @return
//     */
//    public List<Integer> getNearestPoint(SpatialVectorTable spatialTable,double lon, double lat,int limit){
//        List<Integer> result=new ArrayList();
//
//        try {
//
//            String query = SpatialiteUtilities.buildGetNearestPoint(spatialTable.getTableName(), Double.toString(lon),
//                    Double.toString(lat),Integer.toString(limit),spatialTable.getROWID(),Integer.toString(srid));
//                Stmt stmt = dbJava.prepare(query);
//                try {
//                    if (stmt.step()) {
//                        result.add(stmt.column_int(0));
//                    }
//                } finally {
//                    stmt.close();
//                }
//
//
//        }    catch (Exception ex) {
//            GPLog.error(this, null, ex);
//
//        }
//        return result;
//    }



//
//    public route gerRoute(SpatialVectorTable spatialTable,String networkname,double lon1,double lat1,double lon2,double lat2){
//
//        WKBReader wkbReader = new WKBReader();
//        List<Integer> startPointID=this.getNearestPoint(spatialTable,lon1,lat1,1,"32632");
//        List<Integer> endPointID=this.getNearestPoint(spatialTable,lon2,lat2,1,"32632");
//
//        if(startPointID.size()!=1||endPointID.size()!=1){
//            GPLog.androidLog(Log.ERROR,"Error happened while getting start and end pointids");
//            return null;
//        }
//
//        route result=null;
//
//
//        try {
//
//            String query = SpatialiteUtilities.buildRoute(networkname, Integer.toString(startPointID.get(0)),
//                    Integer.toString(endPointID.get(0)));
//            Stmt stmt = dbJava.prepare(query);
//            try {
//                if (stmt.step()) {
//                    try {
//                        double cost=stmt.column_double(13);
//                        byte[] geomBytes = stmt.column_bytes(14);
//                        Geometry geometry = wkbReader.read(geomBytes);
//                        result=new route(cost,geometry);
//                    } catch (java.lang.Exception e) {
//                        GPLog.error(this, "GeometryIterator.next()[wkbReader.read() failed]", e);
//                    }
//
//                }
//            } finally {
//                stmt.close();
//            }
//
//
//        }    catch (Exception ex) {
//            GPLog.error(this, null, ex);
//
//        }
//        return result;
//    }
//


    public List<Integer> getNearestPoint(String pointTable,String rowid,double lon, double lat,int limit,int srid
    ,String node_column){
        List<Integer> result=new ArrayList();

        try {

            String query = SpatialiteUtilities.buildGetNearestPoint(pointTable, Double.toString(lon),
                    Double.toString(lat),Integer.toString(limit),rowid,Integer.toString(srid),node_column);
            Stmt stmt = dbJava.prepare(query);
            try {
                if (stmt.step()) {
                    result.add(stmt.column_int(0));
                }
            } finally {
                stmt.close();
            }


        }    catch (Exception ex) {
            GPLog.error(this, null, ex);

        }
        return result;
    }


    public route gerRoute(String pointTable,String networkname,double lon1,double lat1,double lon2,double lat2,
                          int s_pointid,int e_pointid,int SPATIALITE_VERSION,int table_srid,String node_from,String node_to){

        WKBReader wkbReader = new WKBReader();

        if(s_pointid==-1||e_pointid==-1){
            List<Integer> startPointID=this.getNearestPoint(pointTable,"",lon1,lat1,1,table_srid,node_from);
            List<Integer> endPointID=this.getNearestPoint(pointTable,"",lon2,lat2,1,table_srid,node_to);
            if(startPointID.size()!=1||endPointID.size()!=1){
                GPLog.androidLog(Log.ERROR,"Error happened while getting start and end pointids");
                return null;
            }else{
                s_pointid=startPointID.get(0);
                e_pointid=endPointID.get(0);
            }
        }

        route result=null;


        try {

            String query = SpatialiteUtilities.buildRoute(networkname, Integer.toString(s_pointid),
                    Integer.toString(e_pointid));
            Stmt stmt = dbJava.prepare(query);
            try {
                if (stmt.step()) {
                    try {
                        double cost=0;
                        Geometry geometry=null;
                        if(SPATIALITE_VERSION==5){
                             cost=stmt.column_double(13);
                            byte[] geomBytes = stmt.column_bytes(14);
                            geometry = wkbReader.read(geomBytes);
                        }
                        else
                            {
                            cost=stmt.column_double(4);
                            byte[] geomBytes = stmt.column_bytes(6);
                            geometry = wkbReader.read(geomBytes);
                        }

                        result=new route(cost,geometry);


                    } catch (java.lang.Exception e) {
                        GPLog.error(this, "GeometryIterator.next()[wkbReader.read() failed]", e);
                    }

                }
            } finally {
                stmt.close();
            }


        }    catch (Exception ex) {
            GPLog.error(this, null, ex);

        }
        return result;
    }

    /**
     * Check if a table is enabled to support spatila index
     * @param mTablename Table name to check
     * @return true : supports, False: doe not support
     */
    public boolean SpatialIndexEnabled(String mTablename) {
        boolean result=false;
        try {

            String query = SpatialiteUtilities.buildSpatialIndexEnabled(mTablename);
            Stmt stmt = dbJava.prepare(query);

            try {
                if (stmt.step()) {
                    try {

                       if(stmt.column_int(0)==1)
                           result= true;


                    } catch (java.lang.Exception e) {
                        GPLog.error(this, "SpatialIndexEnabled check failed]", e);
                    }

                }
            } finally {
                stmt.close();
            }


        }    catch (Exception ex) {
            GPLog.error(this, null, ex);
        }
        return result;

    }

    /**
     * Enable Spatial index for a table
     * @param mTablename
     */
    public void enableSpatialIndex(String mTablename,String Geomety_column) {

        try {

            String query = SpatialiteUtilities.buildEnableSpatialIndex(mTablename,Geomety_column);
             dbJava.exec(query, new Callback() {
                @Override
                public void columns(String[] coldata) {

                }

                @Override
                public void types(String[] types) {

                }

                @Override
                public boolean newrow(String[] rowdata) {
                    return false;
                }
            });




        }    catch (Exception ex) {
            GPLog.error(this, null, ex);
        }
    }


    public class route{
        Double Cost_;
        Geometry geom_;
        public route(Double Cost,Geometry geom){
            Cost_=Cost;
            geom_=geom;
        }

        public Geometry getGeom_(){
            return geom_;
        }
        public Double getCost_(){
            return Cost_;
        }
    }


    public void updateLayerStatstics(){
        try {

            String query = "SELECT InvalidateLayerStatistics();\n" +
                    "SELECT UpdateLayerStatistics();";
            dbJava.exec(query, new Callback() {
                @Override
                public void columns(String[] coldata) {

                }

                @Override
                public void types(String[] types) {

                }

                @Override
                public boolean newrow(String[] rowdata) {
                    return false;
                }
            });




        }    catch (Exception ex) {
            GPLog.error(this, null, ex);
        }
    }
}
