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

import com.wire.xenon.backend.models.QualifiedId;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Structure holding qualified users and a Prekey for each of their clients.
 *
 */
public class PreKeys extends HashMap<QualifiedId, HashMap<String, PreKey>> {
    public PreKeys() {
    }

    public PreKeys(ArrayList<PreKey> array, String clientId, QualifiedId userId) {
        super();

        HashMap<String, PreKey> devs = new HashMap<>();
        for (PreKey key : array) {
            devs.put(clientId, key);
        }
        put(userId, devs);
    }

    public int count() {
        int ret = 0;
        for (HashMap<String, PreKey> cls : values())
            ret += cls.size();
        return ret;
    }
}