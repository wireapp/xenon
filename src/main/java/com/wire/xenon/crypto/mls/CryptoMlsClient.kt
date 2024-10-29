package com.wire.xenon.crypto.mls

import com.wire.crypto.client.ClientId
import com.wire.crypto.client.CoreCryptoCentral
import com.wire.crypto.client.MLSClient
import com.wire.crypto.client.MLSGroupId
import com.wire.crypto.client.MlsMessage
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.util.*

class CryptoMlsClient : Closeable {
    private var cryptoCentral: CoreCryptoCentral
    private var mlsClient: MLSClient

    constructor(clientId: String, clientDatabaseKey: String) {
        runBlocking {
            cryptoCentral = CoreCryptoCentral.invoke(
                getDirectoryPath(clientId),
                clientDatabaseKey)
            mlsClient = cryptoCentral.mlsClient(ClientId(clientId))
        }
    }

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

    override fun close() {
        runBlocking { cryptoCentral.close() }
    }
}