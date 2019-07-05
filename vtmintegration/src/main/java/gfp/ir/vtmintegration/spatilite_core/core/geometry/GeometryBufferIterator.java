/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */
package gfp.ir.vtmintegration.spatilite_core.core.geometry;

//import jts.geom.Geometry;

import java.util.Iterator;

import gfp.ir.vtmintegration.geolibrary.database.GPLog;
import gfp.ir.vtmintegration.spatilite_core.util.SpatialiteUtilities;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

/**
 * Class that iterates over Database geometries and doesn't keep everything in memory.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class GeometryBufferIterator implements Iterator<String> {
    private Stmt stmt;
    private String labelText = "";
    private String themeFieldValue;

    /**
     * Returns Label String (if any)
     *
     * @return the label.
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Get the theme field unique value if available, or null.
     *
     * @return the value or null.
     */
    public String getThemeFieldValue(){
        return themeFieldValue;
    }

    /**
     * Builds Label String (if any)
     * <p>
     * Assumes that column 0 is a Geometry, 1 label, 2 theme. The keyword 'dummy' means no label/theme.
     *
     * @param stmt statement being executed
     */
    private void setLabelAndThemeText(Stmt stmt) {
        labelText = "";
        int i = 1;
        int columnCount = 0;
        try {
            if (stmt != null) {
                columnCount = stmt.column_count();
                if (columnCount == 3) {
                    // get the label
                    String labelString = stmt.column_string(1);
                    if (!labelString.equals(SpatialiteUtilities.DUMMY)) {
                        labelText = labelString;
                    }
                    String themeString = stmt.column_string(2);
                    if (!themeString.equals(SpatialiteUtilities.DUMMY)) {
                        themeFieldValue = themeString;
                    }
//                        switch (stmt.column_type(1)) {
//                            case Constants.SQLITE_INTEGER: {
//                                labelText = labelText + stmt.column_int(1);
//                            }
//                            break;
//                            case Constants.SQLITE_FLOAT: {
//                                labelText += String.format("%.5f", stmt.column_double(i));
//                            }
//                            break;
//                            case Constants.SQLITE_BLOB: { // not supported
//                            }
//                            break;
//                            case Constants.SQLITE3_TEXT: {
//                                labelText += stmt.column_string(1);
//                            }
//                            break;
//                        }
                }
            }
        } catch (Exception e) {
            GPLog.error(this, "GeometryIterator.setLabelAndThemeText column_count[" + columnCount + "] column[" + i + "]", e);
        }
    }

    /**
     * Constructor.
     *
     * @param database the database to use.
     * @param query    the query to use.
     */
    public GeometryBufferIterator(Database database, String query) {
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
    public String next() {
        if (stmt == null) {
            GPLog.androidLog(4, "GeometryIterator.next() [stmt=null]");
            return null;
        }
        try {
            String geomBytes = stmt.column_string(0);


            setLabelAndThemeText(stmt);
            return geomBytes;
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
}
