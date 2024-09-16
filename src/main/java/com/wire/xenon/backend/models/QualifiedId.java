package com.wire.xenon.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QualifiedId {
    public QualifiedId(UUID id, String domain) {
        this.id = Objects.requireNonNull(id, "UUID cannot be null");
        this.domain = domain;
    }

    public QualifiedId() {
    }

    @JsonProperty
    @NotNull
    public UUID id;

    @JsonProperty
    public String domain;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QualifiedId)) return false;
        QualifiedId qualifiedId = (QualifiedId) o;
        return Objects.equals(id, qualifiedId.id) && Objects.equals(domain, qualifiedId.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domain);
    }

    @Override
    public String toString() {
        return id + "_" + domain;
    }
}
