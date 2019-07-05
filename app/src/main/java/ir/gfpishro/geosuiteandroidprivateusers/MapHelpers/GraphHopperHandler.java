package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers;

import android.os.AsyncTask;
import android.os.Environment;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.Parameters.Algorithms;
import com.graphhopper.util.Parameters.Routing;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import com.orhanobut.logger.Logger;

import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.PathLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;

/**
 * https://github.com/graphhopper/graphhopper/tree/master/docs/core
 */
public class GraphHopperHandler {
    private static GraphHopper graphHopper;

    static void killHopper() {
        if (graphHopper != null) graphHopper.close();
        graphHopper = null;
    }

    public static GraphHopper getGraphHopper() {
        String path = Environment.getExternalStorageDirectory() + Keys.mapsFolder + Keys.pathGraphHopper;
        try {
            if (graphHopper == null) graphHopper = new LoadGraphStorage(path).execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e, "GraphHopperHandler");
            return null;
        }
        return graphHopper;
    }

    static PathLayer createPathLayer(final org.oscim.map.Map map, final PathWrapper response) {
        PathLayer pathLayer = new PathLayer(map, 0x9900cc33);
        List<GeoPoint> geoPoints = new ArrayList<>();
        PointList pointList = response.getPoints();
        for (int i = 0; i < pointList.getSize(); i++)
            geoPoints.add(new GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)));
        pathLayer.setPoints(geoPoints);
        return pathLayer;
    }

    public static class CalcPath extends AsyncTask<Void, Void, PathWrapper> {
        private final GraphHopper hopper;
        private final double fromLat;
        private final double fromLon;
        private final double toLat;
        private final double toLon;
        private float time;

        CalcPath(final GraphHopper hopper, final double fromLat, final double fromLon,
                 final double toLat, final double toLon) {
            this.hopper = hopper;
            this.fromLat = fromLat;
            this.fromLon = fromLon;
            this.toLat = toLat;
            this.toLon = toLon;
        }

        protected PathWrapper doInBackground(Void... v) {
            StopWatch sw = new StopWatch().start();
            GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon)
                    .setAlgorithm(Algorithms.DIJKSTRA_BI)
                    .setWeighting("fastest")
                    .setVehicle("car");
            req.getHints().put(Routing.INSTRUCTIONS, "false");
            GHResponse resp = hopper.route(req);
            if (resp == null || resp.hasErrors()) return null;
            time = sw.stop().getSeconds();
            return resp.getBest();
        }

        protected void onPostExecute(PathWrapper resp) {
            if (resp != null && !resp.hasErrors()) {
                Logger.d("Graphhopper", "from:" + fromLat + "," + fromLon + " to:" + toLat + ","
                        + toLon + " found path with distance:" + resp.getDistance()
                        / 1000f + ", nodes:" + resp.getPoints().getSize() + ", time:"
                        + time + " " + resp.getDebugInfo());
                Logger.d("", "the route is " + (int) (resp.getDistance() / 100) / 10f
                        + "km long, time:" + resp.getTime() / 60000f + "min, debug:" + time);
            }
        }
    }

    public static class LoadGraphStorage extends AsyncTask<Void, Void, GraphHopper> {
        private String path;

        LoadGraphStorage(String path) {
            this.path = path;
        }

        @Override
        protected GraphHopper doInBackground(Void... voids) {
            File mapsFolder = new File(path);
            if (!mapsFolder.exists()) mapsFolder.mkdirs();
            GraphHopper hopper = new GraphHopper().forMobile();
            try {
                hopper.load(path);
                ((LocationIndexTree) hopper.getLocationIndex()).setMaxRegionSearch(10);
                Logger.d("Graphhopper", "found graph " + hopper.getGraphHopperStorage().toString() + ", nodes:" + hopper.getGraphHopperStorage().getNodes());
                return hopper;
            } catch (Exception e) {
                Logger.e(e, "Graphhopper");
                return null;
            }
        }
    }
}