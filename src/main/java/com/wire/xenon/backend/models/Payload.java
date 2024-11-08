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
import com.fasterxml.jackson.core.type.TypeReference;
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
    public Data data;

    @JsonProperty
    public UUID team;

    // User Mode
    @JsonProperty
    public Connection connection;
    @JsonProperty
    public User user;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = Data.Deserializer.class)
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
                JsonNode node = jp.readValueAsTree();
                if (node.isObject()) {
                    Data data = new Data();
                    data.sender = node.get("sender").asText();
                    data.recipient = node.get("recipient").asText();
                    data.text = node.get("text").asText();
                    data.userIds = jp.getCodec().readValue(node.get("qualified_user_ids").traverse(jp.getCodec()), new TypeReference<List<QualifiedId>>() {});
                    data.users = jp.getCodec().readValue(node.get("users").traverse(jp.getCodec()), new TypeReference<List<User>>() {});
                    data.name = node.get("name").asText();
                    data.creator = node.get("creator").isTextual() ? UUID.fromString(node.get("creator").asText()) : null;
                    data.members = jp.getCodec().readValue(node.get("members").traverse(jp.getCodec()), Members.class);
                    return data;
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
    @JsonDeserialize(using = Members.Deserializer.class)
    public static class Members {
        @JsonProperty
        public List<Member> others;

        /**
         * Custom deserializer to handle Payload.Data.Members being either an object or an array.
         *
         * <p>
         *     In recent versions of the API, "members" is always an object with a field "others" that is an array.
         *     However, in older versions of the API, "members" was just an array of Member objects.
         * </p>
         */
        public static class Deserializer extends StdDeserializer<Members> {
            public Deserializer() {
                this(null);
            }

            Deserializer(Class<?> vc) {
                super(vc);
            }

            @Override
            public Members deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                JsonNode node = jp.readValueAsTree();
                if (node.isObject()) {
                    Members members = new Members();
                    members.others = jp.getCodec().readValue(node.get("others").traverse(jp.getCodec()), new TypeReference<List<Member>>() {});
                    return members;
                } else if (node.isArray()) {
                    Members members = new Members();
                    members.others = jp.getCodec().readValue(node.traverse(jp.getCodec()), new TypeReference<List<Member>>() {});
                    return members;
                } else {
                    throw new RuntimeException("Unable to parse Members as object or array");
                }
            }
        }
    }

}
