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

package com.wire.xenon;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.xenon.assets.IAsset;
import com.wire.xenon.assets.IGeneric;
import com.wire.xenon.backend.models.Conversation;
import com.wire.xenon.backend.models.QualifiedId;
import com.wire.xenon.backend.models.User;
import com.wire.xenon.exceptions.HttpException;
import com.wire.xenon.models.AssetKey;
import com.wire.xenon.models.otr.PreKey;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Thread safe class for postings into this conversation
 */
public interface WireClient extends Closeable {

    Integer KEY_PACKAGES_LOWER_THRESHOLD = 10;
    Integer KEY_PACKAGES_REPLENISH_AMOUNT = 50;

    /**
     * Post a generic message into conversation
     *
     * @param message generic message (Text, Image, File, Reply, Mention, ...)
     * @throws Exception
     */
    void send(IGeneric message) throws Exception;

    /**
     * @param message generic message (Text, Image, File, Reply, Mention, ...)
     * @param userId  ignore all other participants except this user
     * @throws Exception
     */
    void send(IGeneric message, QualifiedId userId) throws Exception;

    /**
     * This method downloads asset from the Backend.
     *
     * @param assetKey        Unique asset identifier (String)
     * @param domain          Domain of the backend hosting the resource (String)
     * @param assetToken      Asset token (null in case of public assets)
     * @param sha256Challenge SHA256 hash code for this asset
     * @param otrKey          Encryption key to be used to decrypt the data
     * @return Decrypted asset data
     * @throws Exception
     */
    byte[] downloadAsset(String assetKey, String domain, String assetToken, byte[] sha256Challenge, byte[] otrKey) throws Exception;

    /**
     * @return Bot ID as UUID
     */
    UUID getId();

    /**
     * Fetch the bot's or user's own user profile information. A bot's profile has the following attributes:
     * <p>
     * id (String): The requester's user ID.
     * name (String): The requester's name.
     * accent_id (Number): The requester's accent colour.
     * assets (Array): The requester's public profile assets (e.g. images).
     *
     * @return the requester's data, either user or bot
     */
    User getSelf() throws HttpException;

    /**
     * @return Conversation ID as Qualified. Id and domain supporting federation
     */
    QualifiedId getConversationId();

    /**
     * @return Device ID as returned by the Wire Backend
     */
    String getDeviceId();

    /**
     * Fetch users' profiles from the Backend
     *
     * @param userIds User IDs (Qualified) that are being requested
     * @return Collection of user profiles (name, accent colour,...)
     * @throws HttpException
     */
    Collection<User> getUsers(Collection<QualifiedId> userIds) throws HttpException;

    /**
     * Fetch users' profiles from the Backend
     *
     * @param userId User ID (Qualified) that are being requested
     * @return User profile (name, accent colour,...)
     * @throws HttpException
     */
    User getUser(QualifiedId userId) throws HttpException;

    /**
     * Fetch conversation details from the Backend
     *
     * @return Conversation details including Conversation ID, Conversation name, List of participants
     * @throws IOException
     */
    Conversation getConversation() throws IOException;

    /**
     * Bots cannot send/receive/accept connect requests. This method can be used when
     * running the sdk as a regular user and you need to
     * accept/reject a connect request.
     *
     * @param user User ID as Qualified
     * @throws Exception
     */
    void acceptConnection(QualifiedId user) throws Exception;

    /**
     * Decrypt Proteus cipher either using existing session or it creates new session from this cipher and decrypts
     *
     * @param userId   Sender's User id
     * @param clientId Sender's Client id
     * @param cypher   Encrypted, Base64 encoded string
     * @return Base64 encoded decrypted text
     * @throws CryptoException
     */
    String decryptProteus(QualifiedId userId, String clientId, String cypher) throws CryptoException;

    /**
     * Decrypt MLS cipher either using existing group.
     *
     * @param mlsGroupId   mls reference of the conversation owning this message
     * @param cypher   Encrypted, Base64 encoded string
     * @return byte array of decrypted text, according to protobuf definition
     */
    byte[] decryptMls(String mlsGroupId, String cypher);

    /**
     * Fetch the default public key (SHA256) of the initialized MLS client and upload it to the backend
     */
    void updateClientWithMlsPublicKey();

    /**
     * Generate (or fetch if available) a specified amount of MLS KeyPackages and upload them to the backend
     *
     * @param keyPackageAmount the amount of key packages to generate and upload
     */
    void uploadMlsKeyPackages(int keyPackageAmount);

    /**
     * Ask the backend for this device to join the specified MLS conversation.
     * THe MLS data for the client needs to be already established and present in the backend.
     * If accepted, the conversation will be marked as joined on the backend and locally in core-crypto storage.
     *
     * @param conversationId the conversation to join
     * @param mlsGroupId the MLS groupId of the conversation to join
     */
    void joinMlsConversation(QualifiedId conversationId, String mlsGroupId);

    /**
     * When a mls-welcome event is received, this method is called to process it.
     * It will create a MLS conversation record in the local core-crypto storage.
     * @param welcome base64 encoded welcome message
     * @return the MLS group id of the conversation
     */
    byte[] processWelcomeMessage(String welcome);

    /**
     * Checks if the number of available key packages is below the threshold and replenishes them if necessary.
     * NOTE: Will make an API call to publish the new key packages if needed.
     */
    void checkAndReplenishKeyPackages();

    /**
     * Invoked by the sdk. Called once when the conversation is created
     *
     * @return Last prekey
     * @throws CryptoException
     */
    PreKey newLastPreKey() throws CryptoException;

    /**
     * Invoked by the sdk. Called once when the conversation is created and then occasionally when number of available
     * keys drops too low
     *
     * @param from  Starting offset
     * @param count Number of keys to generate
     * @return List of prekeys
     * @throws CryptoException
     */
    ArrayList<PreKey> newPreKeys(int from, int count) throws CryptoException;

    /**
     * Uploads previously generated prekeys to BE
     *
     * @param preKeys Pre keys to be uploaded
     * @throws IOException
     */
    void uploadPreKeys(ArrayList<PreKey> preKeys) throws IOException;

    /**
     * Returns the list of available prekeys.
     * If the number is too low (less than 8) you should generate new prekeys and upload them to BE
     *
     * @return List of available prekeys' ids
     */
    ArrayList<Integer> getAvailablePrekeys();

    /**
     * Download publicly available profile picture for the given asset key. This asset is not encrypted
     *
     * @param assetKey Unique asset identifier (String)
     * @param domain   Domain of the backend hosting the resource (String)
     * @return Profile picture binary data
     * @throws Exception
     */
    byte[] downloadProfilePicture(String assetKey, String domain) throws Exception;

    /**
     * Uploads assert to backend. This method is used in conjunction with sendPicture(IGeneric)
     *
     * @param asset Asset to be uploaded
     * @return Assert Key and Asset token in case of private assets
     * @throws Exception
     */
    AssetKey uploadAsset(IAsset asset) throws Exception;

    Conversation createConversation(String name, UUID teamId, List<QualifiedId> users) throws HttpException;

    Conversation createOne2One(UUID teamId, QualifiedId userId) throws HttpException;

    void leaveConversation(QualifiedId userId) throws HttpException;

    void addParticipants(QualifiedId... userIds) throws HttpException;

    void addService(UUID serviceId, UUID providerId) throws HttpException;

    boolean deleteConversation(UUID teamId) throws HttpException;
}
