//
// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

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
import java.util.List;
import java.util.UUID;

/**
 * Part of response of events fetched from the backend, coming either via notification Rest endpoint or
 * web-socket stream.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload {
    @JsonProperty
    @NotNull
    public String type;

    @JsonProperty("qualified_conversation")
    public QualifiedId conversation;

    @JsonProperty("qualified_from")
    public QualifiedId from;

    @JsonProperty
    public String time;

    @JsonProperty
    @JsonDeserialize(using = Data.Deserializer.class)
    public Data data;

    @JsonProperty
    public UUID team;

    // User Mode
    @JsonProperty
    public Connection connection;
    @JsonProperty
    public User user;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty
        public String sender;
        @JsonProperty
        public String recipient;
        @JsonProperty
        public String text;
        // Depending on event type, users can be represented via complete object or just array of qualified ids
        @JsonProperty("qualified_user_ids")
        public List<QualifiedId> userIds;
        @JsonProperty
        public List<User> users;
        @JsonProperty
        public String name;

        // User Mode
        @JsonProperty
        public UUID creator;
        @JsonProperty
        public Members members;

        /**
         * Custom deserializer to handle Payload.Data being either a string value or an object.
         *
         * <p>
         *     When getting notification events, most of them will have something inside the "data" field.
         *     On receiving a MLS message, "data" will be a string value, while in all other cases "data" will
         *     be deserialized as an object.
         * </p>
         */
        public static class Deserializer extends StdDeserializer<Data> {
            public Deserializer() {
                this(null);
            }

            Deserializer(Class<?> vc) {
                super(vc);
            }

            @Override
            public Data deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                JsonNode node = jp.getCodec().readTree(jp);
                if (node.isObject()) {
                    return jp.readValueAs(Data.class);
                } else if (node.isTextual()) {
                    Data data = new Data();
                    data.text = node.asText();
                    return data;
                } else {
                    throw new RuntimeException("Unable to parse Data as object or string");
                }
            }
        }
    }

    // User Mode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Connection {
        @JsonProperty
        public String status;

        @JsonProperty("qualified_from")
        public QualifiedId from;

        @JsonProperty("qualified_to")
        public QualifiedId to;

        @JsonProperty("qualified_conversation")
        public QualifiedId conversation;
    }

    // User Mode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Members {
        @JsonProperty
        public List<Member> others;
    }

}
