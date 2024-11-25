package com.wire.xenon.crypto.mls

import com.wire.crypto.CoreCrypto
import com.wire.crypto.CoreCryptoCallbacks
import com.wire.crypto.client.Ciphersuites
import com.wire.crypto.client.ClientId
import com.wire.crypto.client.CommitBundle
import com.wire.crypto.client.GroupInfo
import com.wire.crypto.client.MLSClient
import com.wire.crypto.client.MLSGroupId
import com.wire.crypto.client.MLSKeyPackage
import com.wire.crypto.client.MlsMessage
import com.wire.crypto.client.PlaintextMessage
import com.wire.crypto.client.Welcome
import com.wire.crypto.coreCryptoDeferredInit
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.io.File
import java.nio.file.Paths
import java.util.*

class CryptoMlsClient(private val clientId: String, clientDatabaseKey: String) : Closeable {
    private var coreCrypto: CoreCrypto
    private var mlsClient: MLSClient

    private companion object {
        private const val KEYSTORE_NAME = "keystore"
    }

    init {
        runBlocking {
            val clientDirectoryPath = getDirectoryPath(clientId = clientId)
            val path = "$clientDirectoryPath/$KEYSTORE_NAME"

            File(clientDirectoryPath).mkdirs()

            coreCrypto = coreCryptoDeferredInit(
                path = path,
                key = clientDatabaseKey
            )
            coreCrypto.setCallbacks(callbacks = CoreCryptoCallbacks())
            mlsClient = MLSClient(cc = coreCrypto).apply {
                mlsInit(id = ClientId(clientId), Ciphersuites.DEFAULT)
            }
        }
    }

    fun getId(): String = clientId

    fun getCoreCryptoClient(): MLSClient = mlsClient

    private fun getDirectoryPath(clientId: String): String = "mls/$clientId"

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
        runBlocking { coreCrypto.close() }
    }

    /**
     * <p>
     *     This function wipes current client MLS folder
     * <p>
     * <p>
     *     - Closes CoreCrypto
     *     - Verify if path exists and is a directory
     *     - Deletes files and folder recursively
     * <p>
     * <p>Note(CoreCrypto)</p>
     * <p>
     *     There is an issue with `wipe()` function from `CoreCrypto`
     *     When ticket [WPB-14514] is done we can then update and use it instead of deleting
     *     the folder ourselves.
     * </p>
     */
    fun wipe() {
        this.close()
        runBlocking {
            val path = Paths.get(getDirectoryPath(clientId = clientId))
            val file = path.toFile()

            if (file.exists() && file.isDirectory) {
                file.deleteRecursively()
            }
        }
    }
}

/**
 * <p>
 *     Currently used for initializing CoreCrypto, but there is efforts on removing the necessity on newer
 *     versions of CoreCrypto.
 * <p>
 */
private class CoreCryptoCallbacks : CoreCryptoCallbacks {

    override suspend fun authorize(conversationId: ByteArray, clientId: ByteArray): Boolean = true

    override suspend fun userAuthorize(
        conversationId: ByteArray,
        externalClientId: ByteArray,
        existingClients: List<ByteArray>
    ): Boolean = true

    override suspend fun clientIsExistingGroupUser(
        conversationId: ByteArray,
        clientId: ByteArray,
        existingClients: List<ByteArray>,
        parentConversationClients: List<ByteArray>?
    ): Boolean = true
}
