/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.geojson.geojson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"type", "features"})
public class FeatureCollection extends GeoJSON {
    private final Feature[] features;
    
    @JsonCreator
    public FeatureCollection(@JsonProperty("features") Feature[] features) {
        super();
        this.features = features;
    }

    public Feature[] getFeatures() {
        return features;
    }
}
