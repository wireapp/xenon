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
    private long expires;

    public ImageAsset(UUID messageId, byte[] imageData, String mime) throws Exception {
        super(messageId, mime, imageData);
        this.imageData = imageData;
    }

    public ImageAsset(UUID messageId, byte[] imageData) throws Exception {
        this(messageId, imageData, Util.extractMimeType(imageData));
    }

    @Override
    public Messages.GenericMessage createGenericMsg() {
        Messages.GenericMessage.Builder ret = Messages.GenericMessage.newBuilder()
                .setMessageId(getMessageId().toString());

        Messages.Asset.RemoteData.Builder remoteData = Messages.Asset.RemoteData.newBuilder()
                .setOtrKey(ByteString.copyFrom(getOtrKey()))
                .setSha256(ByteString.copyFrom(getSha256()));

        if (getAssetToken() != null) {
            remoteData.setAssetToken(getAssetToken());
        }

        if (getAssetKey() != null) {
            remoteData.setAssetId(getAssetKey());
        }

        if (getDomain() != null) {
            remoteData.setAssetDomain(getDomain());
        }

        Messages.Asset.Builder asset = Messages.Asset.newBuilder()
                .setExpectsReadConfirmation(isReadReceiptsEnabled())
                .setUploaded(remoteData);

        if (expires > 0) {
            Messages.Ephemeral.Builder ephemeral = Messages.Ephemeral.newBuilder()
                    .setAsset(asset)
                    .setExpireAfterMillis(expires);

            return ret
                    .setEphemeral(ephemeral)
                    .build();
        }
        return ret
                .setAsset(asset)
                .build();
    }

    public byte[] getImageData() {
        return imageData;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

}
