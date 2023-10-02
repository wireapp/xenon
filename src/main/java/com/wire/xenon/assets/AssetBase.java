package com.wire.xenon.assets;


import com.google.protobuf.ByteString;
import com.waz.model.Messages;
import com.wire.xenon.tools.Util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;

public abstract class AssetBase implements IAsset, IGeneric {
    static private final SecureRandom random = new SecureRandom();
    protected final String mimeType;
    protected UUID messageId;
    protected byte[] encBytes;
    protected byte[] otrKey;
    protected String assetKey;
    protected String assetToken;
    protected byte[] sha256;
    protected boolean isPublic = false;
    protected String retention = "persistent";
    protected boolean readReceiptsEnabled = true;
    protected String domain;
    protected long expires;

    public AssetBase(UUID messageId, String mimeType) {
        this.messageId = messageId;
        this.mimeType = mimeType;
    }

    public AssetBase(UUID messageId, String mimeType, byte[] bytes) throws Exception {
        this(messageId, mimeType);

        otrKey = new byte[32];
        random.nextBytes(otrKey);

        byte[] iv = new byte[16];
        random.nextBytes(iv);

        encBytes = Util.encrypt(otrKey, bytes, iv);
        sha256 = MessageDigest.getInstance("SHA-256").digest(encBytes);
    }

    @Override
    public Messages.GenericMessage createGenericMsg() {
        // Remote
        Messages.Asset.RemoteData.Builder remote = Messages.Asset.RemoteData.newBuilder()
                .setOtrKey(ByteString.copyFrom(getOtrKey()))
                .setSha256(ByteString.copyFrom(getSha256()));

        // Only set token on private assets
        if (getAssetToken() != null) {
            remote.setAssetToken(getAssetToken());
        }

        if (getAssetKey() != null) {
            remote.setAssetId(getAssetKey());
        }

        if (getDomain() != null) {
            remote.setAssetDomain(getDomain());
        }

        Messages.Asset.Builder asset = Messages.Asset.newBuilder()
                .setExpectsReadConfirmation(isReadReceiptsEnabled())
                .setUploaded(remote);

        Messages.GenericMessage.Builder ret = Messages.GenericMessage.newBuilder()
                .setMessageId(getMessageId().toString());

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
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    @Override
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
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

    public boolean isReadReceiptsEnabled() {
        return readReceiptsEnabled;
    }

    public void setReadReceiptsEnabled(boolean readReceiptsEnabled) {
        this.readReceiptsEnabled = readReceiptsEnabled;
    }

    public byte[] getOtrKey() {
        return otrKey;
    }

    public void setOtrKey(byte[] otrKey) {
        this.otrKey = otrKey;
    }

    public byte[] getSha256() {
        return sha256;
    }

    public void setSha256(byte[] sha256) {
        this.sha256 = sha256;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }
}
