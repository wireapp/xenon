package com.wire.xenon;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.crypto.client.Ciphersuite;
import com.wire.xenon.assets.IAsset;
import com.wire.xenon.assets.IGeneric;
import com.wire.xenon.backend.KeyPackageUpdate;
import com.wire.xenon.backend.models.*;
import com.wire.xenon.crypto.Crypto;
import com.wire.xenon.crypto.mls.CryptoMlsClient;
import com.wire.xenon.exceptions.HttpException;
import com.wire.xenon.models.AssetKey;
import com.wire.xenon.models.otr.*;
import com.wire.xenon.tools.Logger;
import com.wire.xenon.tools.Util;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;

public class WireClientBase implements WireClient {
    protected final WireAPI api;
    protected final Crypto crypto;
    protected final CryptoMlsClient cryptoMlsClient;
    protected final NewBot state;
    protected Devices devices = null;

    public WireClientBase(WireAPI api, Crypto crypto, CryptoMlsClient cryptoMlsClient, NewBot state) {
        this.api = api;
        this.crypto = crypto;
        this.cryptoMlsClient = cryptoMlsClient;
        this.state = state;
    }

    @Override
    public void send(IGeneric message) throws Exception {
        postGenericMessage(message);
    }

    @Override
    public void send(IGeneric message, QualifiedId userId) throws Exception {
        postGenericMessage(message, userId);
    }

    @Override
    public UUID getId() {
        return state.id;
    }

    @Override
    public String getDeviceId() {
        return state.client;
    }

    @Override
    public QualifiedId getConversationId() {
        return state.conversation.id;
    }

    @Override
    public void close() throws IOException {
        if (crypto != null) {
            crypto.close();
        }
        if (cryptoMlsClient != null) {
            cryptoMlsClient.close();
        }
    }

    /**
     * Encrypt whole message for participants in the Proteus conversation.
     * Implements the fallback for the 412 error code and missing
     * devices.
     *
     * @param generic generic message to be sent
     * @throws Exception CryptoBox exception
     */
    protected void postGenericMessage(IGeneric generic) throws Exception {
        byte[] content = generic.createGenericMsg().toByteArray();

        // Try to encrypt the msg for those devices that we have the session already
        Recipients encrypt = encrypt(content, getAllDevices());
        OtrMessage msg = new OtrMessage(getDeviceId(), encrypt);

        Devices res = api.sendMessage(msg, false);
        if (!res.hasMissing()) {
            // Fetch preKeys for the missing devices from the Backend
            PreKeys preKeys = api.getPreKeys(res.missing);

            Logger.debug("Fetched %d preKeys for %d devices. Bot: %s", preKeys.count(), res.size(), getId());

            // Encrypt msg for those devices that were missing. This time using preKeys
            encrypt = crypto.encrypt(preKeys, content);
            msg.add(encrypt);

            // reset devices so they could be pulled next time
            devices = null;

            res = api.sendMessage(msg, true);
            if (!res.hasMissing()) {
                Logger.error(String.format("Failed to send otr message to %d devices. Bot: %s",
                        res.size(),
                        getId()));
            }
        }
    }

    /**
     * Encrypt and send message to a specific user. Proteus only,.
     * @param userId the user to send the message to
     * @param generic generic message to be sent
     * @throws Exception CryptoBox exception
     */
    protected void postGenericMessage(IGeneric generic, QualifiedId userId) throws Exception {
        // Try to encrypt the msg for those devices that we have the Proteus session already
        Missing all = getAllDevices();
        Missing missing = new Missing();
        for (QualifiedId u : all.toUserIds()) {
            if (userId.equals(u)) {
                Collection<String> clients = all.toClients(u);
                missing.add(u, clients);
            }
        }

        byte[] content = generic.createGenericMsg().toByteArray();

        Recipients encrypt = encrypt(content, missing);
        OtrMessage msg = new OtrMessage(getDeviceId(), encrypt);

        Devices res = api.sendPartialMessage(msg, userId);
        if (!res.hasMissing()) {
            // Fetch preKeys for the missing devices from the Backend
            PreKeys preKeys = api.getPreKeys(res.missing);

            Logger.debug("Fetched %d preKeys for %d devices. Bot: %s", preKeys.count(), res.size(), getId());

            // Encrypt msg for those devices that were missing. This time using preKeys
            encrypt = crypto.encrypt(preKeys, content);
            msg.add(encrypt);

            // reset devices so they could be pulled next time
            devices = null;

            res = api.sendMessage(msg, true);
            if (!res.hasMissing()) {
                Logger.error(String.format("Failed to send otr message to %d devices. Bot: %s",
                        res.size(),
                        getId()));
            }
        }
    }

    @Override
    public User getSelf() {
        return api.getSelf();
    }

    @Override
    public Collection<User> getUsers(Collection<QualifiedId> userIds) {
        return api.getUsers(userIds);
    }

    @Override
    public User getUser(QualifiedId userId) {
        Collection<User> users = api.getUsers(Collections.singleton(userId));
        return users.iterator().next();
    }

    @Override
    public Conversation getConversation() {
        return api.getConversation();
    }

    @Override
    public void acceptConnection(QualifiedId user) throws Exception {
        api.acceptConnection(user);
    }

    @Override
    public void uploadPreKeys(ArrayList<PreKey> preKeys) throws IOException {
        api.uploadPreKeys((preKeys));
    }

    @Override
    public ArrayList<Integer> getAvailablePrekeys() {
        return api.getAvailablePrekeys(state.client);
    }

    @Override
    public byte[] downloadProfilePicture(String assetKey, String domain) throws HttpException {
        return api.downloadAsset(assetKey, domain, null);
    }

    @Override
    public AssetKey uploadAsset(IAsset asset) throws Exception {
        return api.uploadAsset(asset);
    }

    public Recipients encrypt(byte[] content, Missing missing) throws CryptoException {
        return crypto.encrypt(missing, content);
    }

    @Override
    public String decryptProteus(QualifiedId userId, String clientId, String cypher) throws CryptoException {
        return crypto.decrypt(userId, clientId, cypher);
    }

    @Override
    public byte[] decryptMls(String mlsGroupId, String cypher) {
        return cryptoMlsClient.decrypt(mlsGroupId, cypher);
    }

    @Override
    public void updateClientWithMlsPublicKey() {
        ClientUpdate clientUpdate = new ClientUpdate();
        clientUpdate.mlsPublicKeys = cryptoMlsClient.getPublicKeysToUpload();

        api.uploadClientPublicKey(cryptoMlsClient.getId(), clientUpdate);
    }

    @Override
    public void uploadMlsKeyPackages(int keyPackageAmount) {
        final List<byte[]> keyPackages = cryptoMlsClient.generateKeyPackages(keyPackageAmount);
        KeyPackageUpdate keyPackageUpdate = new KeyPackageUpdate();
        keyPackageUpdate.keyPackages = keyPackages.stream().map(Base64.getEncoder()::encodeToString).toList();

        api.uploadClientKeyPackages(cryptoMlsClient.getId(), keyPackageUpdate);
    }

    @Override
    public void joinMlsConversation(QualifiedId conversationId, String mlsGroupId) {
        if (cryptoMlsClient.conversationExists(mlsGroupId)) {
            Logger.info("Conversation %s already exists, ignore it", conversationId);
            return;
        }
        final byte[] conversationGroupInfo = api.getConversationGroupInfo(conversationId);
        final byte[] commitBundle = cryptoMlsClient.createJoinConversationRequest(conversationGroupInfo);
        api.commitMlsBundle(commitBundle);
        // TODO Add error recovery, maybe a simple 3 times retry on api calls with quadratic backoff

        cryptoMlsClient.markConversationAsJoined(mlsGroupId);
    }

    @Override
    public byte[] processWelcomeMessage(String welcome) {
        checkAndReplenishKeyPackages();
        return cryptoMlsClient.processWelcomeMessage(welcome);
    }

    @Override
    public void checkAndReplenishKeyPackages() {
        if (cryptoMlsClient.validKeyPackageCount() < KEY_PACKAGES_LOWER_THRESHOLD) {
            Logger.info("Too few Key packages, replenish them");
            cryptoMlsClient.generateKeyPackages(KEY_PACKAGES_REPLENISH_AMOUNT);
        }
    }

    @Override
    public PreKey newLastPreKey() throws CryptoException {
        return crypto.newLastPreKey();
    }

    @Override
    public ArrayList<PreKey> newPreKeys(int from, int count) throws CryptoException {
        return crypto.newPreKeys(from, count);
    }

    @Override
    public byte[] downloadAsset(String assetId, String domain, String assetToken, byte[] sha256Challenge, byte[] otrKey)
            throws Exception {
        byte[] cipher = api.downloadAsset(assetId, domain, assetToken);
        byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(cipher);
        if (!Arrays.equals(sha256, sha256Challenge))
            throw new Exception("Failed sha256 check");

        return Util.decrypt(otrKey, cipher);
    }

    @Override
    public Conversation createConversation(String name, UUID teamId, List<QualifiedId> users) throws HttpException {
        return api.createConversation(name, teamId, users);
    }

    @Override
    public Conversation createOne2One(UUID teamId, QualifiedId userId) throws HttpException {
        return api.createOne2One(teamId, userId);
    }

    @Override
    public void leaveConversation(QualifiedId userId) throws HttpException {
        api.leaveConversation(userId);
    }

    @Override
    public void addParticipants(QualifiedId... userIds) throws HttpException {
        api.addParticipants(userIds);
    }

    @Override
    public void addService(UUID serviceId, UUID providerId) throws HttpException {
        api.addService(serviceId, providerId);
    }

    @Override
    public boolean deleteConversation(UUID teamId) throws HttpException {
        return api.deleteConversation(teamId);
    }

    private Missing getAllDevices() throws HttpException {
        return getDevices().missing;
    }

    /**
     * This method will send an empty message to BE and collect the list of missing client ids
     * When empty message is sent the Backend will respond with error 412 and a list of missing clients.
     *
     * @return List of all participants in this conversation and their clientIds
     */
    private Devices getDevices() throws HttpException {
        if (devices == null || devices.hasMissing()) {
            String deviceId = getDeviceId();
            OtrMessage msg = new OtrMessage(deviceId, new Recipients());
            devices = api.sendMessage(msg, false);
        }
        return devices != null ? devices : new Devices();
    }
}
