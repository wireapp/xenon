package com.wire.xenon.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = QualifiedId.Deserializer.class)
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

    /**
     * Custom deserializer to handle a both a fully-qualified id and a simple UUID value.
     *
     * <p>
     *     While newer api all support fully-qualified id (json object with both id and domain)
     *     this classes are also used to deserialize previously stored id.
     *     The deserializer accepts both and in the second case just creates a fully-qualified entity
     *     with "null" (local) as domain.
     * </p>
     */
    public static class Deserializer extends StdDeserializer<QualifiedId> {
        public Deserializer() {
            this(null);
        }

        Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public QualifiedId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            if (node.has("id") && node.has("domain")) {
                UUID id = UUID.fromString(node.get("name").asText());
                String domain = node.get("domain").asText();
                return new QualifiedId(id, domain);
            } else if (node.isTextual()) {
                UUID id = UUID.fromString(node.asText());
                return new QualifiedId(id, null);
            } else {
                throw new RuntimeException("unable to parse");
            }
        }
    }

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
