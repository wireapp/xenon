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
import com.wire.xenon.tools.Logger;
import com.wire.xenon.tools.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

public class Picture implements IGeneric, IAsset {
    static private final SecureRandom random = new SecureRandom();

    private byte[] imageData;
    private String mimeType;
    private int width;
    private int height;
    private int size;
    private byte[] otrKey;
    private byte[] encBytes = null;
    private byte[] sha256;
    private String assetKey;
    private String assetToken;
    private boolean isPublic;
    private String retention = "expiring";
    private UUID messageId = UUID.randomUUID();
    private long expires;

    public Picture(byte[] bytes, String mime) throws IOException {
        imageData = bytes;
        size = bytes.length;
        mimeType = mime;
        BufferedImage bufferedImage = loadBufferImage(bytes);
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
    }

    public Picture(byte[] bytes) throws IOException {
        imageData = bytes;
        mimeType = Util.extractMimeType(imageData);
        size = bytes.length;
        BufferedImage bufferedImage = loadBufferImage(bytes);
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
    }

    public Picture(String url) throws IOException {
        try (InputStream input = new URL(url).openStream()) {
            imageData = Util.toByteArray(input);
        }
        mimeType = Util.extractMimeType(imageData);
        size = imageData.length;
        BufferedImage bufferedImage = loadBufferImage(imageData);
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
    }

    public Picture() {
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getRetention() {
        return retention;
    }

    public void setRetention(String retention) {
        this.retention = retention;
    }

    @Override
    public Messages.GenericMessage createGenericMsg() throws Exception {
        Messages.GenericMessage.Builder ret = Messages.GenericMessage.newBuilder()
                .setMessageId(getMessageId().toString());

        Messages.Asset.ImageMetaData.Builder metaData = Messages.Asset.ImageMetaData.newBuilder()
                .setHeight(height)
                .setWidth(width)
                .setTag("medium");

        Messages.Asset.Original.Builder original = Messages.Asset.Original.newBuilder()
                .setSize(size)
                .setMimeType(mimeType)
                .setImage(metaData);

        Messages.Asset.RemoteData.Builder remoteData = Messages.Asset.RemoteData.newBuilder()
                .setAssetId(assetKey)
                .setOtrKey(ByteString.copyFrom(getOtrKey()))
                .setSha256(ByteString.copyFrom(getSha256()));

        if (assetToken != null)
            remoteData.setAssetToken(assetToken);

        Messages.Asset.Builder asset = Messages.Asset.newBuilder()
                .setUploaded(remoteData)
                .setOriginal(original);

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

    @Override
    public byte[] getEncryptedData() {
        if (encBytes == null) {
            try {
                byte[] iv = new byte[16];
                random.nextBytes(iv);
                encBytes = Util.encrypt(getOtrKey(), imageData, iv);
            } catch (Exception e) {
                Logger.exception("It was not possible to encrypt picture.", e);
            }
        }
        return encBytes;
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public byte[] getOtrKey() {
        if (otrKey == null) {
            otrKey = new byte[32];
            random.nextBytes(otrKey);
        }
        return otrKey;
    }

    public void setOtrKey(byte[] otrKey) {
        this.otrKey = otrKey;
    }

    public byte[] getSha256() throws NoSuchAlgorithmException {
        if (sha256 == null) {
            sha256 = MessageDigest.getInstance("SHA-256").digest(encBytes);
        }
        return sha256;
    }

    public void setSha256(byte[] sha256) {
        this.sha256 = sha256;
    }

    public String getAssetKey() {
        return assetKey;
    }

    public void setAssetKey(String assetKey) {
        this.assetKey = assetKey;
    }

    public String getAssetToken() {
        return assetToken;
    }

    public void setAssetToken(String assetToken) {
        this.assetToken = assetToken;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public UUID getMessageId() {
        return messageId;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    private BufferedImage loadBufferImage(byte[] imageData) throws IOException {
        try (InputStream input = new ByteArrayInputStream(imageData)) {
            return ImageIO.read(input);
        }
    }
}
