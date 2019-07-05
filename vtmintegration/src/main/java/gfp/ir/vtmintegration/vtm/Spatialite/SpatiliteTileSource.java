/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm.Spatialite;

import org.locationtech.jts.geom.Envelope;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapElement;
import org.oscim.core.Tag;
import org.oscim.theme.RenderTheme;
import org.oscim.tiling.TileSource;

import java.io.IOException;
import java.util.List;

import gfp.ir.vtmintegration.analysis.OfflineFeature;
import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;
import jsqlite.Exception;


public abstract class SpatiliteTileSource extends TileSource {

    private SpatialiteDatabaseHandler spatialiteDatabaseHandler;
    private SpatialVectorTable mVectorTable;
    private boolean isValid = false;

    public SpatiliteTileSource(SpatialiteDatabaseHandler spatialiteDatabaseHandler, String Table) {
        try {
            this.spatialiteDatabaseHandler = spatialiteDatabaseHandler;
            if (!this.spatialiteDatabaseHandler.isOpen()) this.spatialiteDatabaseHandler.open();
            List<SpatialVectorTable> vectorTables = this.spatialiteDatabaseHandler.getSpatialVectorTables(false);
            for (int i = 0; i < vectorTables.size(); i++) {
                if (vectorTables.get(i).getTableName().equals(Table)) {
                    mVectorTable = vectorTables.get(i);
                    break;
                }
            }
            if (mVectorTable != null) {
                isValid = true;
                float[] b = mVectorTable.getTableBounds();
                BoundingBox boundingBox = new BoundingBox(b[1], b[0], b[3], b[2]);
                mEnvelope = new Envelope(boundingBox.getMinLongitude(), boundingBox.getMinLatitude(), boundingBox.getMaxLongitude(), boundingBox.getMaxLatitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setMinZoom(int minzoom) {
        if (!isValid)
            return;
        mVectorTable.getStyle().maxZoom = minzoom;
        this.mZoomMin = minzoom;
    }

    public void setMaxZoom(int maxzoom) {
        if (!isValid)
            return;
        mVectorTable.getStyle().maxZoom = maxzoom;
        this.mZoomMax = maxzoom;
    }

    public void enableLable(int stat) {
        if (!isValid)
            return;
        mVectorTable.getStyle().labelvisible = stat;
    }


    public void setLable(String lablefield) {
        if (!isValid)
            return;

        mVectorTable.setLabelField(lablefield);
        mVectorTable.getStyle().labelfield = lablefield;
    }

    public SpatialVectorTable getVectorTable() {

        return mVectorTable;
    }

    public SpatialiteDatabaseHandler getSpatialiteDatabaseHandler() {
        return spatialiteDatabaseHandler;
    }


    public boolean validateLable(String field) {
        if (!isValid)
            return false;

        for (int i = 0; i < mVectorTable.getTableFieldNamesList().size(); i++) {
            if (field.equals(mVectorTable.getTableFieldNamesList().get(i))) {
                setLable(field);
                return true;
            }
        }
        return false;
    }

//    @Override
//    public ITileDataSource getDataSource() {
//        return new SpatiliteTileDataSource(splite,vectortable);
//    }

    /**
     * allow overriding tag handling
     */
    public abstract void decodeTags(MapElement mapElement, Tag[] properties);

    @Override
    public OpenResult open() {
        if (spatialiteDatabaseHandler.isOpen() && spatialiteDatabaseHandler.isValid() && isValid) {
            return OpenResult.SUCCESS;
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        if (spatialiteDatabaseHandler.isOpen() && spatialiteDatabaseHandler.isValid()) {
            try {
                spatialiteDatabaseHandler.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public GeoPoint getCenter() {
        return new GeoPoint(mEnvelope.centre().x, mEnvelope.centre().y);
    }

    Envelope mEnvelope;

    public Envelope getEnvelope() {
        return mEnvelope;
    }

    public abstract RenderTheme getTheme();

    public OfflineFeature getFeatureAt(double x, double y, BoundingBox viewport) {
        return new OfflineFeature(spatialiteDatabaseHandler, mVectorTable, x, y, viewport);
    }
}
