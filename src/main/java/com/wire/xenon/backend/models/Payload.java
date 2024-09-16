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

import javax.validation.constraints.NotNull;
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
    public Qualified conversation;

    @JsonProperty("qualified_from")
    public Qualified from;

    @JsonProperty
    public String time;

    @JsonProperty
    public Data data;

    @JsonProperty
    public UUID team;

    // User Mode
    @JsonProperty
    public Connection connection;

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
        public List<Qualified> userIds;
        @JsonProperty
        public List<User> users;
        @JsonProperty
        public String name;

        // User Mode
        @JsonProperty
        public UUID creator;
        @JsonProperty
        public Members members;
    }

    // User Mode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Connection {
        @JsonProperty
        public String status;

        @JsonProperty("qualified_from")
        public Qualified from;

        @JsonProperty("qualified_to")
        public Qualified to;

        @JsonProperty("qualified_conversation")
        public Qualified conversation;
    }

    // User Mode
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Members {
        @JsonProperty
        public List<Member> others;
    }

}
