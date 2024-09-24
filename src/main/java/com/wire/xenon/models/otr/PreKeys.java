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
import com.wire.xenon.backend.models.QualifiedId;

import java.util.*;

/**
 * Structure holding qualified users and a Prekey for each of their clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreKeys {
    public PreKeys() {
    }

    @JsonProperty("failed_to_list")
    public final List<QualifiedId> failedToList = new ArrayList<>();

    @JsonProperty("qualified_user_client_prekeys")
    public final Map<String, Map<UUID, Map<String, PreKey>>> qualifiedUserClientPrekeys = new HashMap<>();

    public PreKeys(ArrayList<PreKey> array, String clientId, QualifiedId userId) {
        Map<String, PreKey> devs = new HashMap<>();
        for (PreKey key : array) {
            devs.put(clientId, key);
        }
        Map<UUID, Map<String, PreKey>> users = new HashMap<>();
        users.put(userId.id, devs);
        qualifiedUserClientPrekeys.put(userId.domain, users);
    }

    public int count() {
        int ret = 0;
        for (Map<UUID, Map<String, PreKey>> domain : qualifiedUserClientPrekeys.values()) {
            for (Map<String, PreKey> user : domain.values()) {
                ret += user.size();
            }
        }
        return ret;
    }
}