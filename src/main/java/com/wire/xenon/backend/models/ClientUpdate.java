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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientUpdate {
    @JsonProperty("mls_public_keys")
    public MlsPublicKeys mlsPublicKeys;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MlsPublicKeys {
        @JsonProperty("ecdsa_secp256r1_sha256")
        public String ecdsaSecp256r1Sha256;
        @JsonProperty("ecdsa_secp384r1_sha384")
        public String ecdsaSecp384r1Sha384;
        @JsonProperty("ecdsa_secp521r1_sha512")
        public String ecdsaSecp521r1Sha512;
        @JsonProperty("ed25519")
        public String ed25519;
    }
}
