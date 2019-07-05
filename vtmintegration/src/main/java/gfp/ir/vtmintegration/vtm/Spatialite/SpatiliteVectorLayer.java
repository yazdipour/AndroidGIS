package gfp.ir.vtmintegration.vtm.Spatialite;

import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.tile.TileManager;
import org.oscim.layers.tile.VectorTileRenderer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.TileSource;

import gfp.ir.vtmintegration.analysis.OfflineFeature;

public class SpatiliteVectorLayer extends VectorTileLayer {
    public SpatiliteVectorLayer(Map map, TileSource tileSource) {
        super(map, tileSource);
    }

    public SpatiliteVectorLayer(Map map, int cacheLimit) {
        super(map, cacheLimit);
    }

    public SpatiliteVectorLayer(Map map, TileManager tileManager, VectorTileRenderer renderer) {
        super(map, tileManager, renderer);
    }

    public GeoPoint getCenter() {
        if (mTileSource instanceof SpatiliteTileSource)
            return ((SpatiliteTileSource) mTileSource).getCenter();
        return null;
    }

    public OfflineFeature getFeatureAt(double x, double y, BoundingBox viewport) {
        if (mTileSource instanceof SpatiliteTileSource)
            return ((SpatiliteTileSource) mTileSource).getFeatureAt(x, y,viewport);
        return null;
    }

    public OfflineFeature getFeatureAt(GeoPoint p, BoundingBox viewport) {
        return getFeatureAt(p.getLongitude(), p.getLatitude(),viewport);
    }
}
