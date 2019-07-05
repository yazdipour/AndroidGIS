package gfp.ir.vtmintegration.analysis;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import gfp.ir.vtmintegration.geolibrary.database.GPLog;
import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;
import gfp.ir.vtmintegration.spatilite_core.util.SpatialiteUtilities;
import gfp.ir.vtmintegration.vtm.Spatialite.OfflineSearch;
import jsqlite.Exception;
import jsqlite.Stmt;

public class EisAnalysis {

    private SpatialiteDatabaseHandler mSpatiliteDatabaseHandeler;
    private String dbpath;
    private String query = "";

    public EisAnalysis() {
    }

    public EisAnalysis(String dbpath) {
        this.dbpath = dbpath;
        try {
            mSpatiliteDatabaseHandeler = new SpatialiteDatabaseHandler(dbpath);
            mSpatiliteDatabaseHandeler.open();
            List<SpatialVectorTable> vtable = mSpatiliteDatabaseHandeler.getSpatialVectorTables(false);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, String> getAsHashMap(String s, Eis eis) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("geojson", s);
        hashMap.put("eisId", String.valueOf(eis.getId()));
        hashMap.put("eisType", eis.getType());
        hashMap.put("eisMsg", eis.getMsg());
        hashMap.put("eisGeoJson", eis.getGeoJson());
        hashMap.put("q", query);
        return hashMap;
    }

    public HashMap<String, String> getAsHashMap(String s, String pk, String type, String eisGeoJson, String q) {
        Eis eis = new Eis();
        eis.setId(Integer.parseInt(pk));
        eis.setType(type);
        eis.setGeoJson(eisGeoJson);
        query = q;
        return getAsHashMap(s, eis);
    }

    public HashMap<String, String> getRiserByPoint(double x, double y) {
        Eis eis = getEisByPoint(x, y);
        JSONObject result = getRiserByEIS(eis);
        if (result == null) return null;
        return getAsHashMap(result.toString(), eis);
    }

    private JSONObject getRiserByEIS(Eis eis) {
        if (eis != null) {
            query = "select  AsGeoJSON(r.geometry), r.id as pk from gasnet_serviceriser as r ,gasnet_eis as eis where eis.id=" + eis.getId() + " and  st_intersects(r.geometry,eis.geometry) ;\n";
            //we have query here
            OfflineSearch off = new OfflineSearch(dbpath);
            return off.Search(query);
        }
        return null;
    }

    private Eis getEisByPoint(double x, double y) {
        query = SpatialiteUtilities.buildEisQuery(String.valueOf(x), String.valueOf(y));
        return getEisByQuery(query);
    }

    private Eis getEisByRiser(String riserid) {
        query = "select * from gasnet_eis as eis,gasnet_serviceriser as r where r.id=" + riserid + " and st_intersects(eis.geometry,r.Geometry)";
        return getEisByQuery(query);
    }

    private Eis getEisByQuery(String q) {
        Stmt stmt;
        try {
            stmt = mSpatiliteDatabaseHandeler.getDatabase().prepare(q);
            stmt.step();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        int i = 1;
        int columnCount = 0;
        try {
            columnCount = stmt.column_count();
            String geom = stmt.column_string(0);
            if (geom != null && !geom.equals(SpatialiteUtilities.DUMMY)) {
//                    geojson = geom;
            }
            //getting id and type
            Eis eis = new Eis();
            eis.setId(stmt.column_int(1));
            eis.setType(stmt.column_string(3));
            return eis;
        } catch (Exception e) {
            GPLog.error(this, "GeometryIterator.setLabelAndThemeText column_count[" + columnCount + "] column[" + i + "]", e);
        }
        return null;
    }

    public HashMap<String, String> getValveByPoint(double x, double y) {
        Eis eis = getEisByPoint(x, y);
        JSONObject result = getValveByEis(eis);
        if (result == null) return null;
        return getAsHashMap(result.toString(), eis);
    }

    private JSONObject getValveByEis(Eis eis) {
        if (eis != null) {
            query = "select AsGeoJSON(v.geometry), v.id as pk from gasnet_pg_valve as v ";
            switch (eis.getType()) {
                case "A":
                case "B":
                    query += " WHERE v.v_eis1_id= " + eis.getId();
                    break;
                default:
                    query += " WHERE v.v_eis1_id= " + eis.getId() + " or v.v_eis2_id=" + eis.getId();
                    break;
            }
            //we have query here
            OfflineSearch off = new OfflineSearch(dbpath);
            return off.Search(query);
        }
        return null;
    }

    public HashMap<String, String> getParcelByPoint(double x, double y) {
        Eis eis = getEisByPoint(x, y);
        JSONObject result = getParcelByEIS(eis);
        if (result == null) return null;
        return getAsHashMap(result.toString(), eis);
    }

    private JSONObject getParcelByEIS(Eis eis) {
        if (eis != null) {
            query = "select  AsGeoJSON(r.geometry), r.id as pk  from gasnet_parcel as r ,gasnet_eis as eis where eis.id=" + eis.getId() + " and  st_intersects(r.geometry,eis.geometry) ;\n";
            switch (eis.getType()) {
                case "A":
                    break;
                case "B":
                    break;
                default:
                    break;
            }
            //we have query here
            OfflineSearch off = new OfflineSearch(dbpath);
            return off.Search(query);
        }
        return null;
    }
}


