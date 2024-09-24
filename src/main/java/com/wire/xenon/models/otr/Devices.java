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

package com.wire.xenon.models.otr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Devices {
    @JsonProperty
    public final Missing missing = new Missing();

    @JsonProperty
    public final Missing redundant = new Missing();

    @JsonProperty
    public final Missing deleted = new Missing();

    @JsonProperty("failed_to_confirm_clients")
    public final Missing failedToConfirmClients = new Missing();

    @JsonProperty("failed_to_send")
    public final Missing failedToSend = new Missing();

    public boolean hasMissing() {
        return missing.isEmpty();
    }

    public int size() {
        int ret = 0;
        for (ConcurrentHashMap<UUID, Collection<String>> users : missing.values()) {
            for (Collection<String> clients : users.values()) {
                ret += clients.size();
            }
        }
        return ret;
    }
}
