package com.wire.xenon.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureConfig {

    @JsonProperty("mls")
    public MlsResponse mls;

    public static FeatureConfig disabledMls() {
        FeatureConfig featureConfig = new FeatureConfig();
        featureConfig.mls = MlsResponse.disabledMlsResponse();
        return featureConfig;
    }
}
