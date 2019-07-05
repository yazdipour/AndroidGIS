package gfp.ir.vtmintegration.vtm.utils;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import gfp.ir.vtmintegration.geojson.geojson.GeoJSONFactory;
import gfp.ir.vtmintegration.geojson.jts2geojson.GeoJSONReader;

public class utils
{


    public static Envelope getBoundry(String Feature_){
        GeoJSONReader reader = new GeoJSONReader();
        gfp.ir.vtmintegration.geojson.geojson.Feature feature = (gfp.ir.vtmintegration.geojson.geojson.Feature) GeoJSONFactory.create(Feature_);

        Geometry mGeometry = reader.read(feature.getGeometry());
        return mGeometry.getEnvelopeInternal();
    }
}
