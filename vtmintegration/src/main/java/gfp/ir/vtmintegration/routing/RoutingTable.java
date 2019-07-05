/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.routing;

import android.util.Log;

import org.locationtech.jts.geom.Geometry;
import org.oscim.backend.canvas.Color;
import org.oscim.layers.vector.geometries.Style;

import gfp.ir.vtmintegration.geolibrary.database.GPLog;
import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.spatilite_core.core.layers.SpatialVectorTableLayer;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;

public class RoutingTable extends SpatialVectorTableLayer {
    SpatialiteDatabaseHandler splite;
    String networkname;

    Double Cost=.00;
    Geometry geometry=null;
    /**
     * Constructor.
     *
     * @param vectorTable the table to wrap.
     */
    public RoutingTable(SpatialiteDatabaseHandler splite_, SpatialVectorTable vectorTable,String networkname_) {
        super(vectorTable);
        splite=splite_;
        networkname=networkname_;
        init();
    }


    public RoutingTable(SpatialiteDatabaseHandler splite_, SpatialVectorTable vectorTable) {
        super(vectorTable);
        splite=splite_;
        networkname=vectorTable.getTableName()+"_bycar";
        init();
    }

//    public VectorLayer route(Map map,double lon1, double lat1, double lon2, double lat2){
//
////      SpatialiteDatabaseHandler.route route= splite.gerRoute(this.getSpatialVectorTable(),networkname,lon1,lat1,lon2,lat2);
////
////      if(route==null)
////          return null;
////
////        Cost=route.getCost_();
////        geometry=route.getGeom_();
////
////        VectorLayer vl=new VectorLayer(map);
////
////        vl.add(geometry,getStyle());
////
////        vl.update();
////
////        return vl;
//    }




    public Style getStyle(){

        Style.Builder sb = Style.builder()
                .buffer(0.5)
                .fillColor(Color.RED)
                .fillAlpha(0.2f);

        Style style = sb.build();
        return style;

    }


    private void init() {

        if(this.isLine())
        {
            //this.getSpatialVectorTable().getTableName()
            //TODO:check if there routing table exists

            if(splite.RoutingTableExists(networkname)){
                GPLog.androidLog(Log.INFO,"Vitrual routing Table exist, very nice");

            }else{
                GPLog.androidLog(Log.INFO,"Vitrual routing Table does not exist");

                if(this.getSpatialVectorTable().getTableFieldNamesList().contains("node_from")||this.getSpatialVectorTable().getTableFieldNamesList().contains("node_to")){
                    GPLog.androidLog(Log.INFO,"node_from or node_to columns already exsits. please Drop these fields first, We will use these fields so it might make some errors");
                }else{
                    if(splite.buildCreateRoutingNodes(this.getSpatialVectorTable()))
                    {
                        GPLog.androidLog(Log.ERROR,"Error happened while creating Routing nodes");

                    }
                }


                    if(splite.buildVitrualRouting(this.getSpatialVectorTable())){
                        GPLog.androidLog(Log.INFO,"VitrualRouting is generated");

                    }else
                        {
                        GPLog.androidLog(Log.ERROR,"Error happened while creating VitrualRouting");
                         }


            }

        }else{

            GPLog.androidLog(Log.ERROR,"[The input vectortable is not line] GeomType[" +Integer.toBinaryString  (this.getSpatialVectorTable().getGeomType()) + "]");

        }
    }
}
