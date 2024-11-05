package com.wire.xenon.crypto.mls

import com.wire.crypto.client.ClientId
import com.wire.crypto.client.CoreCryptoCentral
import com.wire.crypto.client.GroupInfo
import com.wire.crypto.client.MLSClient
import com.wire.crypto.client.MLSGroupId
import com.wire.crypto.client.MlsMessage
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

    private fun getDirectoryPath(clientId: String): String {
        return "mls/$clientId"
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

    // TODO handle conversation marked as complete, after both welcomeMessage and member-join events have been received,
    // TODO remember checking there are enough key packages
    // https://wearezeta.atlassian.net/wiki/spaces/ENGINEERIN/pages/563053166/Use+case+being+added+to+a+conversation+MLS
    fun welcomeMessage(welcome: ByteArray): ByteArray {
        val welcomeBundle = runBlocking { mlsClient.processWelcomeMessage(Welcome(welcome)) }
        return welcomeBundle.id.value
    }

    fun validKeyPackageCount(): Long {
        val packageCount = runBlocking { mlsClient.validKeyPackageCount() }
        return packageCount.toLong()
    }

    fun createJoinConversationRequest(groupInfo: ByteArray): ByteArray {
        val commitBundle = runBlocking { mlsClient.joinByExternalCommit(GroupInfo(groupInfo)) }
        // TODO serialize commitBundle by doing a sum of its bytearray fields
        return groupInfo
    }

    fun markConversationAsJoined(mlsGroupId: String) {
        val mlsGroupIdBytes: ByteArray = Base64.getDecoder().decode(mlsGroupId)
        val commitBundle = runBlocking { mlsClient.mergePendingGroupFromExternalCommit(MLSGroupId(mlsGroupIdBytes)) }
        // TODO support the possibility of merging returning some decrypted messages ?
    }

    override fun close() {
        runBlocking { cryptoCentral.close() }
    }
}
