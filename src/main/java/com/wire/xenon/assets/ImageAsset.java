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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageAsset extends AssetBase {
    private final byte[] imageData;

    public ImageAsset(UUID messageId, byte[] imageData, String mime) throws Exception {
        super(messageId, mime, imageData);
        this.imageData = imageData;
    }

    public ImageAsset(UUID messageId, byte[] imageData) throws Exception {
        this(messageId, imageData, Util.extractMimeType(imageData));
    }

    public byte[] getImageData() {
        return imageData;
    }
}
