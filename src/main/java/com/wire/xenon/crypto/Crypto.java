package com.wire.xenon.crypto;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.xenon.models.otr.Missing;
import com.wire.xenon.models.otr.PreKey;
import com.wire.xenon.models.otr.PreKeys;
import com.wire.xenon.models.otr.Recipients;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public interface Crypto extends Closeable {
    byte[] getIdentity() throws CryptoException;

    byte[] getLocalFingerprint() throws CryptoException;

    PreKey newLastPreKey() throws CryptoException;

    ArrayList<PreKey> newPreKeys(int from, int count) throws CryptoException;

    Recipients encrypt(PreKeys preKeys, byte[] content) throws CryptoException;

    /**
     * Append cipher to {@code msg} for each device using crypto box session. Ciphers for those devices that still
     * don't have the session will be skipped and those must be encrypted using prekeys:
     *
     * @param missing List of device that are missing
     * @param content Plain text content to be encrypted
     */
    Recipients encrypt(Missing missing, byte[] content) throws CryptoException;

    /**
     * Decrypt cipher either using existing session or it creates new session from this cipher and decrypts
     *
     * @param userId   Sender's User id
     * @param clientId Sender's Client id
     * @param cypher   Encrypted, Base64 encoded string
     * @return Decrypted Base64 encoded string
     * @throws CryptoException throws CryptoException
     */
    String decrypt(UUID userId, String clientId, String cypher) throws CryptoException;

    boolean isClosed();

    void purge() throws IOException;
}
