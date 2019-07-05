package gfp.ir.vtmintegration.vtm.Spatialite;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import gfp.ir.vtmintegration.geolibrary.database.GPLog;
import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.spatilite_core.core.iterator.SpatialiteGeojsonIterator;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;
import gfp.ir.vtmintegration.spatilite_core.util.SpatialiteUtilities;
import jsqlite.Exception;

/**
 * This class is to search spatilite database and return query in geojson form
 */
public class OfflineSearch {


    private boolean isvalid;
    private SpatialVectorTable mVectorTable;
    private SpatialiteDatabaseHandler mSpatiliteDatabaseHandeler;
    List<SpatialVectorTable> vtable;

    /**
     * constractor
     *
     * @param dbpath path of spatilite database from local memory
     */
    public OfflineSearch(String dbpath) {
        try {
            mSpatiliteDatabaseHandeler = new SpatialiteDatabaseHandler(dbpath);
            mSpatiliteDatabaseHandeler.open();
            vtable = mSpatiliteDatabaseHandeler.getSpatialVectorTables(false);
            isvalid = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * returns a JSONOBJECT
     *
     * @param table table to search from
     * @param field field to search from
     * @param value value to search it will search all like '%value%'
     *              <p>
     *              To Search Parcel table:gasnet_parcel field:code_address
     *              To Search Street table:gasnet_street field:name
     *              To Search Street table:gasnet_serviceriser field:r_num
     *              To Search Street table:gasnet_pg_valve field:v_num
     *              To Search Street table:gasnet_bg_valve field:v_num
     * @return JSONOBJECT
     */
    public JSONObject Search(String table, String field, String value) {
        if (isvalid) {
            for (int i = 0; i < vtable.size(); i++) {
                if (vtable.get(i).getTableName().equals(table)) {
                    mVectorTable = vtable.get(i);
                    break;
                }
            }
            if (mVectorTable != null) {
                if (mVectorTable.getTableFieldNamesList().contains(field)) {
                    String query = SpatialiteUtilities.buildSearchQuery(table, field, value);
                    return Search(query);
                } else {
                    GPLog.androidLog(Log.INFO, "The selected field does not exist in tabel" + field + "]");
                }
            } else {
                GPLog.androidLog(Log.INFO, "Table does not exist :" + table + "]");
            }
        }
        return null;
    }

    /**
     * searchs a raw query from database
     *
     * @param Query raw string
     * @return
     */
    public JSONObject Search(String Query) {

        if (isvalid) {
            try {
                SpatialiteGeojsonIterator itr = new SpatialiteGeojsonIterator(mSpatiliteDatabaseHandeler.getDatabase(), Query);
                return itr.getAllFeatures();
            } catch (java.lang.Exception ex) {

            }

        } else {
            GPLog.androidLog(-1, "Spatilite db is not valid");
        }

        return new JSONObject();
    }
}
