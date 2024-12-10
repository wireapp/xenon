package com.wire.xenon.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MlsResponse {
    @JsonProperty("config")
    public MlsConfigResponse config;

    @JsonProperty
    public String status;

    public static MlsResponse disabledMlsResponse() {
        MlsResponse mlsResponse = new MlsResponse();
        mlsResponse.status = "disabled";
        return mlsResponse;
    }

    public boolean isMlsStatusEnabled() {
        return status != null && status.equals("enabled");
    }
}
