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
package gfp.ir.vtmintegration.spatilite_core.util;

import android.util.Log;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;

import java.util.Arrays;
import java.util.List;

import gfp.ir.vtmintegration.geolibrary.database.GPLog;
import gfp.ir.vtmintegration.geolibrary.util.types.EDataType;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;
import jsqlite.Database;
import jsqlite.Stmt;;

/**
 * SpatialiteUtilities class.
 *
 * @author Mark Johnson
 */
@SuppressWarnings("nls")
public class SpatialiteUtilities {
    /**
     * Name of the table field that s used to identify the record.
     */
    public static final String SPATIALTABLE_ID_FIELD = "ROWID"; //$NON-NLS-1$

    /**
     * Array of fields that will be ingored in attributes handling.
     */
    public static String[] IGNORED_FIELDS = {SPATIALTABLE_ID_FIELD, "PK_UID", "_id"};


    public static List<String> reserverSqlWords = Arrays.asList("ABORT", "ACTION", "ADD", "AFTER", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ATTACH", "AUTOINCREMENT", "BEFORE", "BEGIN", "BETWEEN", "BY", "CASCADE", "CASE", "CAST", "CHECK", "COLLATE", "COLUMN", "COMMIT", "CONFLICT", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "DATABASE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DETACH", "DISTINCT", "DROP", "EACH", "ELSE", "END", "ESCAPE", "EXCEPT", "EXCLUSIVE", "EXISTS", "EXPLAIN", "FAIL", "FOR", "FOREIGN", "FROM", "FULL", "GLOB", "GROUP", "HAVING", "IF", "IGNORE", "IMMEDIATE", "IN", "INDEX", "INDEXED", "INITIALLY", "INNER", "INSERT", "INSTEAD", "INTERSECT", "INTO", "IS", "ISNULL", "JOIN", "KEY", "LEFT", "LIKE", "LIMIT", "MATCH", "NATURAL", "NO", "NOT", "NOTNULL", "NULL", "OF", "OFFSET", "ON", "OR", "ORDER", "OUTER", "PLAN", "PRAGMA", "PRIMARY", "QUERY", "RAISE", "RECURSIVE", "REFERENCES", "REGEXP", "REINDEX", "RELEASE", "RENAME", "REPLACE", "RESTRICT", "RIGHT", "ROLLBACK", "ROW", "SAVEPOINT", "SELECT", "SET", "TABLE", "TEMP", "TEMPORARY", "THEN", "TO", "TRANSACTION", "TRIGGER", "UNION", "UNIQUE", "UPDATE", "USING", "VACUUM", "VALUES", "VIEW", "VIRTUAL", "WHEN", "WHERE", "WITH", "WITHOUT");

    /**
     * Name/path separator for spatialite table names.
     */
    public static final String UNIQUENAME_SEPARATOR = "#"; //$NON-NLS-1$

    public static final String DUMMY = "dummy";

    /**
     * Checks if a field needs to be ignored.
     *
     * @param field the field to check.
     * @return <code>true</code> if the field needs to be ignored.
     */
    public static boolean doIgnoreField(String field) {
        for (String ingoredField : SpatialiteUtilities.IGNORED_FIELDS) {
            if (field.equals(ingoredField)) {
                return true;
            }
        }
        field = field.toUpperCase();
        if (reserverSqlWords.contains(field)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a field needs to be ignored.
     *
     * @param field   the field to check.
     * @param pkField a primary key field to be ignored. It can be null
     * @return <code>true</code> if the field needs to be ignored.
     */
    public static boolean doIgnoreField(String field, String pkField) {
        if (field.equals(pkField)) {
            return true;
        }
        for (String ingoredField : SpatialiteUtilities.IGNORED_FIELDS) {
            if (field.equals(ingoredField)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if a primary key field needs to be ignored.
     * <p>
     * Primary keys formed by a single integer field are managed by Sqlite as an alias
     * of ROWID, and a unique value is assigned to this column if omitted on INSERT queries.
     * <p>
     * These PK columns should be excluded when creating a new feature, so that the user
     * does not need to worry about assigning a unique ID.
     * <p>
     * See http://sqlite.org/autoinc.html for more info.
     *
     * @param spatialVectorTable the spatialVectorTable to check.
     * @return The PK field to be ignored (or null if no PK field has to be ignored)
     */
    public static String getIgnoredPkField(SpatialVectorTable spatialVectorTable) {
        try {
            String fields = spatialVectorTable.getPrimaryKeyFields();
            if (!fields.contains(";") && (spatialVectorTable.getTableFieldType(fields) == EDataType.INTEGER)) {
                return fields;
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Build a query to retrieve geometries from a table in a given bound.
     *
     * @param destSrid  the destination srid.
     * @param withRowId if <code>true</code>, the ROWID is added in position 0 of the query.
     * @param table     the table to use.
     * @param n         north bound.
     * @param s         south bound.
     * @param e         east bound.
     * @param w         west bound.
     * @return the query.
     */
    public static String buildGEoJsonGeometriesInBoundsQuery(String destSrid, boolean withRowId, SpatialVectorTable table, double n,
                                                             double s, double e, double w, String inputBoundSrid) {
        boolean doTransform = false;
        if (!table.getSrid().equals(destSrid)) {
            doTransform = true;
        }
        boolean boundDotransform = false;

        if (inputBoundSrid != null)
            boundDotransform = true;

        StringBuilder mbrSb = new StringBuilder();
        if (doTransform || boundDotransform)
            mbrSb.append("ST_Transform(");
        mbrSb.append("BuildMBR(");
        mbrSb.append(w);
        mbrSb.append(",");
        mbrSb.append(n);
        mbrSb.append(",");
        mbrSb.append(e);
        mbrSb.append(",");
        mbrSb.append(s);
        if (doTransform || boundDotransform) {
            mbrSb.append(",");

            if (boundDotransform) {
                mbrSb.append(inputBoundSrid);

            } else {
                mbrSb.append(destSrid);

            }
            mbrSb.append("),");
            mbrSb.append(table.getSrid());
        }
        mbrSb.append(")");
        String mbr = mbrSb.toString();
        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT ");
        if (withRowId) {
            qSb.append(SPATIALTABLE_ID_FIELD).append(",");
        }
        qSb.append("AsGeoJSON(CastToXY(");
        if (doTransform)
            qSb.append("ST_Transform(");
        qSb.append(table.getGeomName());
        if (doTransform) {
            qSb.append(",");
            qSb.append(destSrid);
            qSb.append(")");
        }
        qSb.append("))");
        if (table.getStyle().labelvisible == 1) {
            qSb.append(",");
            qSb.append(table.getStyle().labelfield);
        } else {
            qSb.append(",'" + DUMMY + "'");
        }
        if (table.getStyle().themeField != null) {
            qSb.append(",");
            qSb.append(table.getStyle().themeField);
        } else {
            qSb.append(",'" + DUMMY + "'");
        }
        qSb.append(" FROM ");
        qSb.append("\"").append(table.getTableName()).append("\"");
        // the SpatialIndex would be searching for a square, the ST_Intersects the Geometry
        // the SpatialIndex could be fulfilled, but checking the Geometry could return the result
        // that it is not
        qSb.append(" WHERE ST_Intersects(");
        qSb.append(table.getGeomName());
        qSb.append(", ");
        qSb.append(mbr);
        qSb.append(") = 1 AND ");
        qSb.append(table.getROWID());
        qSb.append("  IN (SELECT ");
        qSb.append(table.getROWID());
        qSb.append(" FROM Spatialindex WHERE f_table_name ='");
        qSb.append(table.getTableName());
        qSb.append("'");
        // if a table has more than 1 geometry, the column-name MUST be given, otherwise no results.
        qSb.append(" AND f_geometry_column = '");
        qSb.append(table.getGeomName());
        qSb.append("'");
        qSb.append(" AND search_frame = ");
        qSb.append(mbr);
        qSb.append(");");
        String q = qSb.toString();
        return q;
    }

    /**
     * Build a query to retrieve geometries from a table in a given bound.
     *
     * @param destSrid  the destination srid.
     * @param withRowId if <code>true</code>, the ROWID is added in position 0 of the query.
     * @param table     the table to use.
     * @param n         north bound.
     * @param s         south bound.
     * @param e         east bound.
     * @param w         west bound.
     * @return the query.
     */
    public static String buildGeometriesInBoundsQuery(String destSrid, boolean withRowId, SpatialVectorTable table, double n,
                                                      double s, double e, double w, String inputBoundSrid) {
        boolean doTransform = false;
        if (!table.getSrid().equals(destSrid)) {
            doTransform = true;
        }
        boolean boundDotransform = false;

        if (inputBoundSrid != null)
            boundDotransform = true;

        StringBuilder mbrSb = new StringBuilder();
        if (doTransform || boundDotransform)
            mbrSb.append("ST_Transform(");
        mbrSb.append("BuildMBR(");
        mbrSb.append(w);
        mbrSb.append(",");
        mbrSb.append(n);
        mbrSb.append(",");
        mbrSb.append(e);
        mbrSb.append(",");
        mbrSb.append(s);
        if (doTransform || boundDotransform) {
            mbrSb.append(",");

            if (boundDotransform) {
                mbrSb.append(inputBoundSrid);

            } else {
                mbrSb.append(destSrid);

            }
            mbrSb.append("),");
            mbrSb.append(table.getSrid());
        }
        mbrSb.append(")");
        String mbr = mbrSb.toString();
        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT ");
        if (withRowId) {
            qSb.append(SPATIALTABLE_ID_FIELD).append(",");
        }
        qSb.append("ST_AsBinary(CastToXY(");
        if (doTransform)
            qSb.append("ST_Transform(");
        qSb.append(table.getGeomName());
        if (doTransform) {
            qSb.append(",");
            qSb.append(destSrid);
            qSb.append(")");
        }
        qSb.append("))");
        if (table.getStyle().labelvisible == 1) {
            qSb.append(",");
            qSb.append(table.getStyle().labelfield);
        } else {
            qSb.append(",'" + DUMMY + "'");
        }
        if (table.getStyle().themeField != null) {
            qSb.append(",");
            qSb.append(table.getStyle().themeField);
        } else {
            qSb.append(",'" + DUMMY + "'");
        }
        qSb.append(" FROM ");
        qSb.append("\"").append(table.getTableName()).append("\"");
        // the SpatialIndex would be searching for a square, the ST_Intersects the Geometry
        // the SpatialIndex could be fulfilled, but checking the Geometry could return the result
        // that it is not
        qSb.append(" WHERE ST_Intersects(");
        qSb.append(table.getGeomName());
        qSb.append(", ");
        qSb.append(mbr);
        qSb.append(") = 1 AND ");
        qSb.append(table.getROWID());
        qSb.append("  IN (SELECT ");
        qSb.append(table.getROWID());
        qSb.append(" FROM Spatialindex WHERE f_table_name ='");
        qSb.append(table.getTableName());
        qSb.append("'");
        // if a table has more than 1 geometry, the column-name MUST be given, otherwise no results.
        qSb.append(" AND f_geometry_column = '");
        qSb.append(table.getGeomName());
        qSb.append("'");
        qSb.append(" AND search_frame = ");
        qSb.append(mbr);
        qSb.append(");");
        String q = qSb.toString();
        return q;
    }

    /**
     * Build a query to retrieve geometries from a table in a given bound.
     *
     * @param destSrid  the destination srid.
     * @param withRowId if <code>true</code>, the ROWID is added in position 0 of the query.
     * @param table     the table to use.
     * @param n         north bound.
     * @param s         south bound.
     * @param e         east bound.
     * @param w         west bound.
     * @return the query.
     */
    public static String buildGeometriesInBoundsQuery(String destSrid, boolean withRowId, SpatialVectorTable table, double n,
                                                      double s, double e, double w) {
        boolean doTransform = false;
        if (!table.getSrid().equals(destSrid)) {
            doTransform = true;
        }
        StringBuilder mbrSb = new StringBuilder();
        if (doTransform)
            mbrSb.append("ST_Transform(");
        mbrSb.append("BuildMBR(");
        mbrSb.append(w);
        mbrSb.append(",");
        mbrSb.append(n);
        mbrSb.append(",");
        mbrSb.append(e);
        mbrSb.append(",");
        mbrSb.append(s);
        if (doTransform) {
            mbrSb.append(",");
            mbrSb.append(destSrid);
            mbrSb.append("),");
            mbrSb.append(table.getSrid());
        }
        mbrSb.append(")");
        String mbr = mbrSb.toString();
        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT ");
        if (withRowId) {
            qSb.append(SPATIALTABLE_ID_FIELD).append(",");
        }
        qSb.append("ST_AsBinary(CastToXY(");
        if (doTransform)
            qSb.append("ST_Transform(");
        qSb.append(table.getGeomName());
        if (doTransform) {
            qSb.append(",");
            qSb.append(destSrid);
            qSb.append(")");
        }
        qSb.append("))");
        if (table.getStyle().labelvisible == 1) {
            qSb.append(",");
            qSb.append(table.getStyle().labelfield);
        } else {
            qSb.append(",'" + DUMMY + "'");
        }
        if (table.getStyle().themeField != null) {
            qSb.append(",");
            qSb.append(table.getStyle().themeField);
        } else {
            qSb.append(",'" + DUMMY + "'");
        }
        qSb.append(" FROM ");
        qSb.append("\"").append(table.getTableName()).append("\"");
        // the SpatialIndex would be searching for a square, the ST_Intersects the Geometry
        // the SpatialIndex could be fulfilled, but checking the Geometry could return the result
        // that it is not
        qSb.append(" WHERE ST_Intersects(");
        qSb.append(table.getGeomName());
        qSb.append(", ");
        qSb.append(mbr);
        qSb.append(") = 1 AND ");
        qSb.append(table.getROWID());
        qSb.append("  IN (SELECT ");
        qSb.append(table.getROWID());
        qSb.append(" FROM Spatialindex WHERE f_table_name ='");
        qSb.append(table.getTableName());
        qSb.append("'");
        // if a table has more than 1 geometry, the column-name MUST be given, otherwise no results.
        qSb.append(" AND f_geometry_column = '");
        qSb.append(table.getGeomName());
        qSb.append("'");
        qSb.append(" AND search_frame = ");
        qSb.append(mbr);
        qSb.append(");");
        String q = qSb.toString();
        return q;
    }

    /**
     * Get the query to run for a bounding box intersection to retrieve features.
     * <p>
     * <p>This assures that the first element of the query is
     * the id field for the record as defined in {@link SpatialiteUtilities#SPATIALTABLE_ID_FIELD}
     * and the last one the geometry.
     *
     * @param boundsSrid   the srid of the bounds requested.
     * @param spatialTable the {@link SpatialVectorTable} to query.
     * @param n            north bound.
     * @param s            south bound.
     * @param e            east bound.
     * @param w            west bound.
     * @return the query to run to get all fields.
     */
    public static String getBboxIntersectingFeaturesQuery(String boundsSrid, SpatialVectorTable spatialTable, double n,
                                                          double s, double e, double w) {
        String query = null;
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
        sbQ.append(",ST_AsBinary(CastToXY(");
        if (doTransform)
            sbQ.append("ST_Transform(");
        sbQ.append(spatialTable.getGeomName());
        if (doTransform) {
            sbQ.append(",");
            sbQ.append(boundsSrid);
            sbQ.append(")");
        }
        sbQ.append("))");
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

        query = sbQ.toString();
        return query;
    }

    /**
     * Get the query to run for a bounding box intersection to retrieve features.
     * <p>
     * <p>This assures that the first element of the query is
     * the id field for the record as defined in {@link SpatialiteUtilities#SPATIALTABLE_ID_FIELD}
     * and the last one the geometry.
     *
     * @param resultSrid   the requested srid.
     * @param spatialTable the {@link SpatialVectorTable} to query.
     * @return the query to run to get the last inserted feature.
     */
    public static String getLastInsertedFeatureQuery(String resultSrid, SpatialVectorTable spatialTable) {
        String query = null;
        boolean doTransform = false;
        String fieldNamesList = SPATIALTABLE_ID_FIELD;
        // List of non-blob fields
        for (String field : spatialTable.getTableFieldNamesList()) {
            boolean ignore = doIgnoreField(field);
            if (!ignore)
                fieldNamesList += "," + field;
        }
        if (!spatialTable.getSrid().equals(resultSrid)) {
            doTransform = true;
        }
        StringBuilder sbQ = new StringBuilder();
        sbQ.append("SELECT ");
        sbQ.append(fieldNamesList);
        sbQ.append(",ST_AsBinary(CastToXY(");
        if (doTransform)
            sbQ.append("ST_Transform(");
        sbQ.append(spatialTable.getGeomName());
        if (doTransform) {
            sbQ.append(",");
            sbQ.append(resultSrid);
            sbQ.append(")");
        }
        sbQ.append("))");
        sbQ.append(" FROM \"").append(spatialTable.getTableName());
        sbQ.append("\" order by " + SPATIALTABLE_ID_FIELD + " desc limit 1");
        sbQ.append(");");

        query = sbQ.toString();
        return query;
    }


    /**
     * Collects bounds and center as wgs84 4326.
     * - Note: use of getEnvelopeInternal() insures that, after transformation,
     * -- possible false values are given - since the transformed result might not be square
     *
     * @param srid              the source srid.
     * @param centerCoordinate  the coordinate array to fill with the center.
     * @param boundsCoordinates the coordinate array to fill with the bounds as [w,s,e,n].
     */
    public static void collectBoundsAndCenter(Database sqlite_db, String srid, double[] centerCoordinate,
                                              double[] boundsCoordinates) {
        String centerQuery = "";
        try {
            Stmt centerStmt = null;
            double bounds_west = boundsCoordinates[0];
            double bounds_south = boundsCoordinates[1];
            double bounds_east = boundsCoordinates[2];
            double bounds_north = boundsCoordinates[3];
            /*
            SELECT ST_Transform(BuildMBR(14121.000000,187578.000000,467141.000000,48006927.000000,23030),4326);
             SRID=4326;POLYGON((
             -7.364919057793379 1.69098037889473,
             -3.296335497384673 1.695910088657131,
             -131.5972302288043 89.99882674963366,
             -131.5972302288043 89.99882674963366,
             -7.364919057793379 1.69098037889473))
            SELECT MbrMaxX(ST_Transform(BuildMBR(14121.000000,187578.000000,467141.000000,48006927.000000,23030),4326));
            -3.296335
            */
            try {
                WKBReader wkbReader = new WKBReader();
                StringBuilder centerBuilder = new StringBuilder();
                centerBuilder.append("SELECT ST_AsBinary(CastToXY(ST_Transform(MakePoint(");
                // centerBuilder.append("select AsText(ST_Transform(MakePoint(");
                centerBuilder.append("(" + bounds_west + " + (" + bounds_east + " - " + bounds_west + ")/2), ");
                centerBuilder.append("(" + bounds_south + " + (" + bounds_north + " - " + bounds_south + ")/2), ");
                centerBuilder.append(srid);
                centerBuilder.append("),4326))) AS Center,");
                centerBuilder.append("ST_AsBinary(CastToXY(ST_Transform(BuildMBR(");
                centerBuilder.append("" + bounds_west + "," + bounds_south + ", ");
                centerBuilder.append("" + bounds_east + "," + bounds_north + ", ");
                centerBuilder.append(srid);
                centerBuilder.append("),4326))) AS Envelope ");
                // centerBuilder.append("';");
                centerQuery = centerBuilder.toString();
                // GPLog.androidLog(-1, "SpatialiteUtilities.collectBoundsAndCenter Bounds[" +
                // centerQuery + "]");
                centerStmt = sqlite_db.prepare(centerQuery);
                if (centerStmt.step()) {
                    byte[] geomBytes = centerStmt.column_bytes(0);
                    Geometry geometry = wkbReader.read(geomBytes);
                    Coordinate coordinate = geometry.getCoordinate();
                    centerCoordinate[0] = coordinate.x;
                    centerCoordinate[1] = coordinate.y;
                    geomBytes = centerStmt.column_bytes(1);
                    geometry = wkbReader.read(geomBytes);
                    Envelope envelope = geometry.getEnvelopeInternal();
                    boundsCoordinates[0] = envelope.getMinX();
                    boundsCoordinates[1] = envelope.getMinY();
                    boundsCoordinates[2] = envelope.getMaxX();
                    boundsCoordinates[3] = envelope.getMaxY();
                }
            } catch (Exception e) {
                GPLog.error("SpatialiteUtilities", ".collectBoundsAndCenter Bounds[" + centerQuery + "]", e);
            } finally {
                if (centerStmt != null)
                    centerStmt.close();
            }
        } catch (Exception e) {
            GPLog.error("SpatialiteUtilities", "[" + sqlite_db.getFilename() + "] sql[" + centerQuery + "]", e);
        }
    }


    public static String buildFieldDistinctValues(String table, String Field) {

//         select distinct layer from Reprojected limit  5
        StringBuilder dstctfield = new StringBuilder();

        dstctfield.append("select distinct ");
        dstctfield.append(Field);
        dstctfield.append(" from ");
        dstctfield.append(table);


        String q = dstctfield.toString();
        return q;
    }


    /**
     * Generates a create route sql query. this only works with version 5 of spatilite and above
     *
     * @param table     the name of the Spatial Table or Spatial View representing the underlying Network.
     *                  Note: in this case we actually used a Spatial View.
     * @param node_from name of the column (in the above Table or View) expected to contain node-from values.
     * @param nodeto    name of the column (in the above Table or View) expected to contain node-to values.
     * @param geom      name of the column (in the above Table or View) expected to contain Linestrings.
     *                  In the case of a Logical Network: a NULL should be passed..
     * @return string query
     */
    public static String buildCreateRouting(String table, String node_from
            , String nodeto, String geom) {
        StringBuilder dstctfield = new StringBuilder();
        try {
//        SELECT CreateRouting('byfoot_data', 'byfoot', 'roads_vw', 'node_from', 'nodeto', 'geom', NULL);


            dstctfield.append("select CreateRouting( ");
            dstctfield.append(" '" + table + "_data', ");
            dstctfield.append(" '" + table + "_bycar', ");
            dstctfield.append(" '" + table + "', ");
            dstctfield.append(" '" + node_from + "', ");
            dstctfield.append(" '" + nodeto + "', ");
            dstctfield.append(" '" + geom + "', ");
            dstctfield.append(" NULL); ");


        } catch (Exception e) {
            GPLog.error("SpatialiteUtilities", "[" + table + "] sql[" + dstctfield.toString() + "]", e);
        }
        String q = dstctfield.toString();
        return q;
    }


    /**
     * SELECT CreateRoutingNodes(NULL, 'table_name', 'geom', 'node_from', 'node_to');
     * rebuild the missing NodeFrom and NodeTo definitions from a valid Network by calling the CreateRoutingNodes() SQL function.
     *
     * @param table_name name of the Spatial Table.
     * @param node_from  name of the column to be added to the above Table and populated with appropriate NodeFrom IDs.
     * @param nodeto     name of the column to be added to the above Table and populated with appropriate NodeTo IDs.
     *                   Note: both NodeFrom and NodeTo columns should not be already defined in the above Table.
     * @param geom       name of the column (in the above Table) containing Linestrings.
     * @return sql qury
     */
    public static String buildCreateRoutingNodes(String table_name, String node_from
            , String nodeto, String geom) {
        StringBuilder dstctfield = new StringBuilder();
        try {
//        SELECT CreateRouting('byfoot_data', 'byfoot', 'roads_vw', 'node_from', 'nodeto', 'geom', NULL);


            dstctfield.append("select CreateRoutingNodes(NULL, ");
            dstctfield.append(" '" + table_name + "', ");
            dstctfield.append(" '" + geom + "', ");
            dstctfield.append(" '" + node_from + "', ");
            dstctfield.append(" '" + nodeto + "' ");
            dstctfield.append("); ");


        } catch (Exception e) {
            GPLog.error("SpatialiteUtilities", "[buildCreateRoutingNodes] sql[" + dstctfield.toString() + "]", e);
        }
        String q = dstctfield.toString();
        return q;
    }


    public static String buildTableExists(String table_name, String type
            , String like) {
        StringBuilder dstctfield = new StringBuilder();
        try {
//            SELECT count(*)
//            FROM sqlite_master
//            WHERE type = 'table' AND name='' AND sql LIKE '%VirtualRouting(%';

            dstctfield.append("select count(*) FROM sqlite_master WHERE ");
            if (type != null)
                dstctfield.append(" type = '" + type + "' AND ");

            if (table_name != null)
                dstctfield.append(" name = '" + table_name + "' AND ");

            if (like != null)
                dstctfield.append(" sql LIKE '%VirtualRouting(%'; ");


        } catch (Exception e) {
            GPLog.error("SpatialiteUtilities", "[buildTableExists] sql[" + dstctfield.toString() + "]", e);
        }
        String q = dstctfield.toString();
        return q;
    }

    public static String buildGetNearestPoint(String tablename, String lat
            , String lon, String limit, String id, String road_srid, String node_column) {
        StringBuilder dstctfield = new StringBuilder();
        try {

//            SELECT id_node FROM knn k, nodes p
//            WHERE f_table_name = 'nodes'
//            AND ref_geometry = MakePoint(0,0)
//            AND max_items = 1 limit 1;


//
//            select r.* from
//                    (SELECT * FROM knn k WHERE f_table_name = 'roads'
//                            AND ref_geometry = Transform(MakePoint(14.011871,41.739952,4326),32632)
//                            AND max_items = 1) as knn join roads as r on r.rowid=knn.fid

            dstctfield.append("select r." + node_column + " from (SELECT * FROM knn k WHERE f_table_name = '" + tablename + "' ");
            if (road_srid.equals("-1")) {
                dstctfield.append(" AND ref_geometry = MakePoint(" + lon + "," + lat + ") ");

            } else {
                dstctfield.append(" AND ref_geometry = Transform(MakePoint(" + lon + "," + lat + ",4326)," + road_srid + ") ");

            }
            dstctfield.append(" AND max_items = 1) as knn join roads as r on r.rowid=knn.fid;");


//            dstctfield.append("SELECT "+id+" FROM knn k, "+tablename+" p ");
//            dstctfield.append(" WHERE f_table_name = '" + tablename + "' ");
//            dstctfield.append(" AND ref_geometry = MakePoint("+lon+","+lat+")");
//            dstctfield.append("AND max_items = 1 limit "+limit+";");


        } catch (Exception e) {
            GPLog.error("SpatialiteUtilities", "[buildGetNearestPoint] sql[" + dstctfield.toString() + "]", e);
        }
        String q = dstctfield.toString();
        GPLog.androidLog(Log.INFO, "[buildGetNearestPoint] sql[" + q + "]");

        return q;
    }


    public static String buildRoute(String networkname, String NodeFrom
            , String NodeTo) {
        StringBuilder dstctfield = new StringBuilder();
        try {

//        SELECT *
//                FROM byfoot
//        WHERE NodeFrom = 178731 AND NodeTo = 183286;


            dstctfield.append("SELECT *,ST_AsBinary(ST_Transform(geometry,4326)) as gemetry_trans FROM  " + networkname + " p ");
            dstctfield.append(" WHERE NodeFrom =  " + NodeFrom + " ");
            dstctfield.append(" AND NodeTo = " + NodeTo + ";");


        } catch (Exception e) {
            GPLog.error("SpatialiteUtilities", "[buildRoute] sql[" + dstctfield.toString() + "]", e);
        }
        String q = dstctfield.toString();
        GPLog.androidLog(Log.INFO, "[buildRoute] sql[" + q + "]");

        return q;
    }

    /**
     * Generate Sql command to check if spatila index is enabled on a table
     *
     * @param tablename name of table o chekc
     * @return sql command as String
     */
    public static String buildSpatialIndexEnabled(String tablename) {
        StringBuilder dstctfield = new StringBuilder();

        try {

//            select spatial_index_enabled from vector_layers
            dstctfield.append("select spatial_index_enabled from vector_layers ");
            dstctfield.append(" where table_name='" + tablename + "'; ");


        } catch (Exception e) {
            GPLog.error("SpatialiteUtilities", "[buildSpatialIndexEnabled] sql[" + dstctfield.toString() + "]", e);
        }
        String q = dstctfield.toString();
        GPLog.androidLog(Log.INFO, "[buildSpatialIndexEnabled] sql[" + q + "]");

        return q;
    }

    /**
     * Generates Spatial index enable query
     *
     * @param mTablename      Name of table to generate spatial index
     * @param Geomerty_Column Geometry column to enable spatial index
     * @return String which is query
     */
    public static String buildEnableSpatialIndex(String mTablename, String Geomerty_Column) {

        StringBuilder dstctfield = new StringBuilder();

        try {

//         SELECT CreateSpatialIndex('roads','geometry');

            dstctfield.append("SELECT CreateSpatialIndex('" + mTablename + "','" + Geomerty_Column + "'); ");


        } catch (Exception e) {
            GPLog.error("buildEnableSpatialIndex", "[buildEnableSpatialIndex] sql[" + dstctfield.toString() + "]", e);
        }
        String q = dstctfield.toString();
        GPLog.androidLog(Log.INFO, "[buildEnableSpatialIndex] sql[" + q + "]");

        return q;
    }


    public static String buildSearchQuery(String mTablename, String Column, String value) {
        StringBuilder dstctfield = new StringBuilder();
        try {
            String fields = "AsGeoJSON(geometry), id as pk, * ";
//            switch (mTablename){
//                case "gasnet_parcel":
//                case "gasnet_street":
//                    fields=" AsGeoJSON(geometry), "+ Column;
            dstctfield.append("SELECT " + fields + " from " + mTablename + " where  " + Column + " like '%" + value + "%' limit 50");
        } catch (Exception e) {
            GPLog.error("buildOfflineQuery", "[buildOfflineQuery] sql[" + dstctfield.toString() + "]", e);
        }
        String q = dstctfield.toString();
        GPLog.androidLog(Log.INFO, "[buildEnableSpatialIndex] sql[" + q + "]");
        return q;
    }

    public static String buildEisQuery(String x, String y) {
        String q="select * from gasnet_eis where st_intersects(geometry,st_geomfromtext('Point("+x+" "+y+" )'))";
        GPLog.androidLog(Log.INFO, "[buildEnableSpatialIndex] sql[" + q + "]");
        return q;
    }


}