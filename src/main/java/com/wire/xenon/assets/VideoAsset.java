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

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;

public class VideoAsset implements IGeneric, IAsset {
    static private final SecureRandom random = new SecureRandom();

    private final UUID messageId;
    private final byte[] encBytes;
    private final byte[] otrKey = new byte[32];
    private final String mimeType;
    private String assetKey;
    private String assetToken;
    private String retention = "persistent";

    public VideoAsset(byte[] bytes, String mime, UUID messageId) throws Exception {
        this.messageId = messageId;
        this.mimeType = mime;

        random.nextBytes(otrKey);

        byte[] iv = new byte[16];
        random.nextBytes(iv);

        encBytes = Util.encrypt(otrKey, bytes, iv);
    }

    @Override
    public Messages.GenericMessage createGenericMsg() throws Exception {
        Messages.Asset.RemoteData.Builder remote = Messages.Asset.RemoteData.newBuilder()
                .setOtrKey(ByteString.copyFrom(otrKey))
                .setSha256(ByteString.copyFrom(MessageDigest.getInstance("SHA-256").digest(getEncryptedData())))
                .setAssetId(assetKey);

        // Only set token on private assets
        if (assetToken != null) {
            remote.setAssetToken(assetToken);
        }

        Messages.Asset asset = Messages.Asset.newBuilder()
                .setUploaded(remote.build())
                .build();

        return Messages.GenericMessage.newBuilder()
                .setMessageId(getMessageId().toString())
                .setAsset(asset)
                .build();
    }

    public void setAssetKey(String assetKey) {
        this.assetKey = assetKey;
    }

    public void setAssetToken(String assetToken) {
        this.assetToken = assetToken;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getRetention() {
        return retention;
    }

    public void setRetention(String retention) {
        this.retention = retention;
    }

    @Override
    public byte[] getEncryptedData() {
        return encBytes;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public UUID getMessageId() {
        return messageId;
    }
}
