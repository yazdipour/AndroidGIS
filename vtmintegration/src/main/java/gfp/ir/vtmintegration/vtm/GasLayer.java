package gfp.ir.vtmintegration.vtm;

import org.oscim.core.MapElement;
import org.oscim.core.Tag;
import org.oscim.theme.RenderTheme;
import org.oscim.tiling.ITileDataSource;

import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.vtm.Spatialite.SpatiliteTileDataSource;
import gfp.ir.vtmintegration.vtm.Spatialite.SpatiliteTileSource;

public class GasLayer extends SpatiliteTileSource {

    public enum FIELDS {
        DIAMETER("diameter"),
        ID("id"),
        FTYPE("F_TYPE"),
        LAYER("layer"),
        BgPipe_TABLE("gasnet_bg_gaspipe"),
        Parcel_PLAQUE("pa_plaque"),
        Parcel_RISERGISCODEMAINE("r_giscodemain"),
        Parcel_CODEADDRESS("code_address"),
        Parcel_MARKETING("pa_marketing"),
        PgPipe_TABLE("gasnet_pg_gaspipe"),
        Parcel_TABLE("gasnet_parcel"),
        R_NUM("r_num"),
        Riser_TABLE("gasnet_serviceriser"),
        PgPoint_TABLE("gasnet_pg_point");

        String label;

        FIELDS(String lbl) {
            label = lbl;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static SpatialiteDatabaseHandler spatialiteDatabaseHandler;

    public static void initSpatialiteDatabaseHandler(String dbPath) {
        if(spatialiteDatabaseHandler != null) return;
        try {
            spatialiteDatabaseHandler = new SpatialiteDatabaseHandler(dbPath);
        } catch (Exception ignored) {
        }
    }

    public GasLayer(FIELDS tableName,
                    FIELDS style,
                    FIELDS label,
                    int minZoom,
                    int maxZoom) {
        super(spatialiteDatabaseHandler, tableName.toString());
        getVectorTable().getStyle().themeField = style.toString();
        setMaxZoom(maxZoom);
        setMinZoom(minZoom);
        if (label == null) return;
        setLable(label.toString());
        enableLable(1);
    }

    @Override
    public RenderTheme getTheme() {
        return null;
    }

    @Override
    public void decodeTags(MapElement mapElement, Tag[] properties) {
    }

    @Override
    public ITileDataSource getDataSource() {
        return new SpatiliteTileDataSource(getSpatialiteDatabaseHandler(), getVectorTable(), this);
    }
}
