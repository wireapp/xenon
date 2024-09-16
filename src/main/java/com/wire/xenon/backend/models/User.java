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

import java.util.ArrayList;

/**
 * User representation in the payload of user focussed notifications.
 * <p>
 * Events inside notifications and different endpoints have different subsets of these fields
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonProperty("qualified_id")
    public Qualified id;

    @JsonProperty
    public String name;

    @JsonProperty("accent_id")
    public int accent;

    @JsonProperty
    public String handle;

    @JsonProperty
    public Service service;

    @JsonProperty
    public String email;

    @JsonProperty
    public ArrayList<Asset> assets;
}
