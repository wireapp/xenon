package com.wire.xenon.crypto.mls

import com.wire.crypto.client.ClientId
import com.wire.crypto.client.CommitBundle
import com.wire.crypto.client.CoreCryptoCentral
import com.wire.crypto.client.GroupInfo
import com.wire.crypto.client.MLSClient
import com.wire.crypto.client.MLSGroupId
import com.wire.crypto.client.MLSKeyPackage
import com.wire.crypto.client.MlsMessage
import com.wire.crypto.client.PlaintextMessage
import com.wire.crypto.client.Welcome
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.util.*

class CryptoMlsClient : Closeable {
    private var cryptoCentral: CoreCryptoCentral
    private var mlsClient: MLSClient
    private val clientId: String

    constructor(clientId: String, clientDatabaseKey: String) {
        runBlocking {
            cryptoCentral = CoreCryptoCentral.invoke(
                getDirectoryPath(clientId),
                clientDatabaseKey)
            mlsClient = cryptoCentral.mlsClient(ClientId(clientId))
        }
        this.clientId = clientId
    }

    fun getId(): String = clientId
    fun getCoreCryptoClient(): MLSClient = mlsClient

    private fun getDirectoryPath(clientId: String): String {
        return "mls/$clientId"
    }

    fun encrypt(mlsGroupId: String, plainMessage: ByteArray): ByteArray? {
        val mlsGroupIdBytes: ByteArray = Base64.getDecoder().decode(mlsGroupId)
        val encryptedMessage = runBlocking { mlsClient.encryptMessage(MLSGroupId(mlsGroupIdBytes), PlaintextMessage(plainMessage)) }
        return encryptedMessage.value
    }

    // Group id needs to be fetched by API given qualified id of the conversation or maybe also create a cache/table, then take base64 string and byte array
    // encryptedMessage is payload.data (string) -> decodeBase64Bytes (take the string as base64 and then get byte array)
    fun decrypt(mlsGroupId: String, encryptedMessage: String): ByteArray? {
        val mlsGroupIdBytes: ByteArray = Base64.getDecoder().decode(mlsGroupId)
        val encryptedMessageBytes: ByteArray = Base64.getDecoder().decode(encryptedMessage)
        val decryptedMessage = runBlocking { mlsClient.decryptMessage(MLSGroupId(mlsGroupIdBytes), MlsMessage(encryptedMessageBytes)) }
        return decryptedMessage.message
    }

    fun getPublicKey(): ByteArray {
        val publicKey = runBlocking { mlsClient.getPublicKey() }
        return publicKey.value
    }

    fun generateKeyPackages(amount: Int): List<ByteArray> {
        val keyPackages = runBlocking { mlsClient.generateKeyPackages(amount.toUInt()) }
        return keyPackages.map { it.value }
    }

    /**
     * Process a welcome message, adding this client to a conversation, and return the group id.
     */
    fun processWelcomeMessage(welcome: String): ByteArray {
        val welcomeBytes: ByteArray = Base64.getDecoder().decode(welcome)
        val welcomeBundle = runBlocking { mlsClient.processWelcomeMessage(Welcome(welcomeBytes)) }
        return welcomeBundle.id.value
    }

    fun validKeyPackageCount(): Long {
        val packageCount = runBlocking { mlsClient.validKeyPackageCount() }
        return packageCount.toLong()
    }

    fun conversationExists(mlsGroupId: String): Boolean {
        val mlsGroupIdBytes: ByteArray = Base64.getDecoder().decode(mlsGroupId)
        return runBlocking { mlsClient.conversationExists(MLSGroupId(mlsGroupIdBytes)) }
    }

    /**
     * Create a request to join a conversation.
     * Needs to be followed by a call to markConversationAsJoined() to complete the process.
     */
    fun createJoinConversationRequest(groupInfo: ByteArray): ByteArray {
        val commitBundle = runBlocking { mlsClient.joinByExternalCommit(GroupInfo(groupInfo)) }
        return parseBundleIntoSingleByteArray(commitBundle)
    }

    /**
     * Return the CommitBundle data as a single byte array, in a specific order.
     * The order is: commit, groupInfoBundle, welcome (optional).
     * The created bundle will be pushed in this format to the backend to request joining a conversation.
     *
     * @param bundle the CommitBundle to parse
     */
    private fun parseBundleIntoSingleByteArray(bundle: CommitBundle): ByteArray {
        return bundle.commit.value + bundle.groupInfoBundle.payload.value + (bundle.welcome?.value ?: ByteArray(0))
    }

    /**
     * Completes the process of joining a conversation.
     * To be called after createJoinConversationRequest(), and having a successful response from the backend
     * while uploading the commitBundle.
     */
    fun markConversationAsJoined(mlsGroupId: String) {
        val mlsGroupIdBytes: ByteArray = Base64.getDecoder().decode(mlsGroupId)
        runBlocking { mlsClient.mergePendingGroupFromExternalCommit(MLSGroupId(mlsGroupIdBytes)) }
        // TODO support the possibility of merging returning some decrypted messages ?
    }

    /**
     * Alternative way to add a member to a conversation.
     * Instead of creating a join request accepted by the new client, this method directly adds a member to a conversation,
     * and returns a welcome message to be sent to the new member.
     */
    fun addMemberToConversation(mlsGroupId: String, keyPackages: List<ByteArray>): ByteArray? {
        val mlsGroupIdBytes: ByteArray = Base64.getDecoder().decode(mlsGroupId)
        val commitBundle = runBlocking { mlsClient.addMember(MLSGroupId(mlsGroupIdBytes), keyPackages.map { MLSKeyPackage(it) }) }
        return commitBundle.welcome?.value;
    }

    fun acceptLatestCommit(mlsGroupId: String) {
        val mlsGroupIdBytes: ByteArray = Base64.getDecoder().decode(mlsGroupId)
        runBlocking { mlsClient.commitAccepted(MLSGroupId(mlsGroupIdBytes)) }
    }

    override fun close() {
        runBlocking { cryptoCentral.close() }
    }
}
