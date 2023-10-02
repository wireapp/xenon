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

package com.wire.xenon.assets;

import com.google.protobuf.ByteString;
import com.waz.model.Messages;
import com.wire.xenon.tools.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class FileAsset extends AssetBase {
    public FileAsset(File file, String mimeType, UUID messageId) throws Exception {
        super(messageId, mimeType, readFile(file));
    }

    public FileAsset(byte[] bytes, String mimeType, UUID messageId) throws Exception {
        super(messageId, mimeType, bytes);
    }

    public FileAsset(UUID messageId, String mimeType) {
        super(messageId, mimeType);
    }

    public FileAsset(String assetKey, String assetToken, byte[] sha256, byte[] otrKey, UUID messageId) {
        super(messageId, null);
        this.assetKey = assetKey;
        this.assetToken = assetToken;
        this.sha256 = sha256;
        this.otrKey = otrKey;
    }

    private static byte[] readFile(File file) throws IOException {
        byte[] bytes;
        try (FileInputStream input = new FileInputStream(file)) {
            bytes = Util.toByteArray(input);
        }
        return bytes;
    }
}
