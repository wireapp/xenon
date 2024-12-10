package com.wire.xenon.crypto.mls

import com.wire.crypto.CoreCrypto
import com.wire.crypto.CoreCryptoCallbacks
import com.wire.crypto.client.Ciphersuite
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
import com.wire.xenon.backend.models.QualifiedId
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.io.File
import java.nio.file.Paths
import java.util.*

class CryptoMlsClient (private val clientId: String, private val userId: QualifiedId, private val ciphersuite: Int, clientDatabaseKey: String) : Closeable {
    private var coreCrypto: CoreCrypto
    private var mlsClient: MLSClient

    private companion object {
        private const val KEYSTORE_NAME = "keystore"
        private const val DEFAULT_CIPHERSUITE_IDENTIFIER = 1
    }

    constructor(clientId: String, userId: QualifiedId, clientDatabaseKey: String) : this(clientId, userId, DEFAULT_CIPHERSUITE_IDENTIFIER, clientDatabaseKey)

    init {
        runBlocking {
            val clientDirectoryPath = getDirectoryPath()
            val path = "$clientDirectoryPath/$KEYSTORE_NAME"

            File(clientDirectoryPath).mkdirs()

            coreCrypto = coreCryptoDeferredInit(
                path = path,
                key = clientDatabaseKey
            )
            coreCrypto.setCallbacks(callbacks = CoreCryptoCallbacks())
            mlsClient = MLSClient(cc = coreCrypto).apply {
                mlsInit(id = ClientId(getCoreCryptoId()), Ciphersuites(setOf(getMlsCipherSuiteName(ciphersuite))))
            }
        }
    }

    fun getId(): String = clientId
    fun getCoreCryptoClient(): MLSClient = mlsClient

    // Fully qualified id for the client, allowing to push key packages to the backend
    private fun getCoreCryptoId(): String = "${userId.id}:$clientId@${userId.domain}"
    private fun getDirectoryPath(): String = "mls/$clientId"

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
        val publicKey = runBlocking { mlsClient.getPublicKey(getMlsCipherSuiteName(ciphersuite)) }
        return publicKey.value
    }

    fun generateKeyPackages(amount: Int): List<ByteArray> {
        val keyPackages = runBlocking { mlsClient.generateKeyPackages(
            amount = amount.toUInt(),
            ciphersuite = getMlsCipherSuiteName(ciphersuite)
        ) }
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
        val packageCount = runBlocking { mlsClient.validKeyPackageCount(getMlsCipherSuiteName(ciphersuite)) }
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
     * This method wipes current client MLS folder.
     *
     * - Closes CoreCrypto
     * - Verify if path exists and is a directory
     * - Deletes files and folder recursively
     *
     * Note(CoreCrypto): There is an issue with `wipe()` function from `CoreCrypto` when ticket [WPB-14514] is done,
     * we can then update and use it instead of deleting the folder ourselves.
     */
    fun wipe() {
        this.close()
        runBlocking {
            val path = Paths.get(getDirectoryPath())
            val file = path.toFile()

            if (file.exists() && file.isDirectory) {
                file.deleteRecursively()
            }
        }
    }

    private fun getMlsCipherSuiteName(code: Int): Ciphersuite {
        return when (code) {
            DEFAULT_CIPHERSUITE_IDENTIFIER -> Ciphersuite.DEFAULT
            2 -> Ciphersuite.MLS_128_DHKEMP256_AES128GCM_SHA256_P256
            3 -> Ciphersuite.MLS_128_DHKEMX25519_CHACHA20POLY1305_SHA256_Ed25519
            4 -> Ciphersuite.MLS_256_DHKEMX448_AES256GCM_SHA512_Ed448
            5 -> Ciphersuite.MLS_256_DHKEMP521_AES256GCM_SHA512_P521
            6 -> Ciphersuite.MLS_256_DHKEMX448_CHACHA20POLY1305_SHA512_Ed448
            7 -> Ciphersuite.MLS_256_DHKEMP384_AES256GCM_SHA384_P384
            else -> Ciphersuite.DEFAULT
        }
    }
}

/**
 * Dummy CoreCrypto Callbacks.
 *
 * Currently used for initializing CoreCrypto, but there is efforts on removing the necessity on newer versions of CoreCrypto.
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
