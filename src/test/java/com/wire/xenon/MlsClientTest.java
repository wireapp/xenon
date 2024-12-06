package com.wire.xenon;

import com.wire.crypto.CoreCryptoException;
import com.wire.xenon.backend.models.QualifiedId;
import com.wire.xenon.crypto.mls.CryptoMlsClient;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MlsClientTest {

    @Test
    public void testMlsClientInitialization() {
        QualifiedId user1 = new QualifiedId(UUID.randomUUID(), "wire.com");
        String client1 = "alice1_" + UUID.randomUUID();
        CryptoMlsClient mlsClient = new CryptoMlsClient(client1, user1, "pwd");
        assert mlsClient != null;
        mlsClient.close();

        CryptoMlsClient mlsSameClient = new CryptoMlsClient(client1, user1, "pwd");
        assert mlsSameClient != null;
        assert mlsSameClient.getId().equals(mlsClient.getId());

        final byte[] publicKey = mlsSameClient.getPublicKey();
        assert publicKey.length > 10;

        final List<byte[]> keyPackages = mlsSameClient.generateKeyPackages(10);
        assert keyPackages.size() == 10;
        mlsSameClient.close();
    }

    @Test
    public void testMlsClientFailOnDifferentPassword(){
        String client1 = "alice1_" + UUID.randomUUID();
        QualifiedId user1 = new QualifiedId(UUID.randomUUID(), "wire.com");
        CryptoMlsClient mlsClient = new CryptoMlsClient(client1, user1, "pwd");
        assert mlsClient != null;
        mlsClient.close();

        assertThrows(CoreCryptoException.class, () -> new CryptoMlsClient(client1, user1, "WRONG_PASSWORD"));
    }

    @Test
    public void testMlsClientCreateConversationAndEncrypt() throws IOException {
        QualifiedId user1 = new QualifiedId(UUID.randomUUID(), "wire.com");
        String client1 = "alice1_" + UUID.randomUUID();
        // Group ID in base64 format, copied from a real one
        String groupIdBase64 = "AAEAAliWyGZ3/FqGpDPZdcuLQ0UAYW50YS53aXJlLmxpbms=";

        // GroupInfo of a real conversation, stored in a binary test file
        InputStream inputStream = new FileInputStream("src/test/resources/tmp.bin");
        byte[] groupInfo = inputStream.readAllBytes();

        // Create a new client and join the conversation
        CryptoMlsClient mlsClient = new CryptoMlsClient(client1, user1, "pwd");
        assert !mlsClient.conversationExists(groupIdBase64);
        final byte[] commitBundle = mlsClient.createJoinConversationRequest(groupInfo);
        assert commitBundle.length > groupInfo.length;
        mlsClient.markConversationAsJoined(groupIdBase64);
        assert mlsClient.conversationExists(groupIdBase64);

        // Encrypt a message for the joined conversation
        String plainMessage = UUID.randomUUID().toString();
        final byte[] encryptedMessage = mlsClient.encrypt(groupIdBase64, plainMessage.getBytes());
        assert encryptedMessage.length > 10;
        final String encryptedBase64Message = Base64.getEncoder().encodeToString(encryptedMessage);

        // assertThrows cannot check for exception messages, so had to use try-catch and assert we have the correct error
        try {
            mlsClient.decrypt(groupIdBase64, encryptedBase64Message);
            throw new IllegalArgumentException("Decryption should fail");
        } catch (Exception e) {
            // Unfortunately it is not possible for a single client to decrypt a message it encrypted itself
            // By getting the duplicated message exception we know that the encryption and decryption works
            // but we cannot attest that the decrypted message is the same as the original
            assert e.getMessage().contains("DuplicateMessage: We already decrypted this message once");
        }
    }


    @Test
    public void testMlsClientsEncryptAndDecrypt() throws IOException {
        QualifiedId user1 = new QualifiedId(UUID.randomUUID(), "wire.com");
        String client1 = "alice1_" + UUID.randomUUID();
        // Group ID in base64 format, copied from a real one
        String groupIdBase64 = "AAEAAliWyGZ3/FqGpDPZdcuLQ0UAYW50YS53aXJlLmxpbms=";

        // GroupInfo of a real conversation, stored in a binary test file
        InputStream inputStream = new FileInputStream("src/test/resources/tmp.bin");
        byte[] groupInfo = inputStream.readAllBytes();

        // Create a new client and join the conversation
        CryptoMlsClient mlsClient = new CryptoMlsClient(client1, user1, "pwd");
        assert !mlsClient.conversationExists(groupIdBase64);
        final byte[] commitBundle = mlsClient.createJoinConversationRequest(groupInfo);
        assert commitBundle.length > groupInfo.length;
        mlsClient.markConversationAsJoined(groupIdBase64);
        assert mlsClient.conversationExists(groupIdBase64);

        // Create a second client and make the first client invite the second one
        QualifiedId user2 = new QualifiedId(UUID.randomUUID(), "wire.com");
        String client2 = "bob1_" + UUID.randomUUID();
        CryptoMlsClient mlsClient2 = new CryptoMlsClient(client2, user2, "pwd");
        assert !mlsClient2.conversationExists(groupIdBase64);
        final List<byte[]> keyPackages = mlsClient2.generateKeyPackages(1);
        final byte[] welcome = mlsClient.addMemberToConversation(groupIdBase64, keyPackages);
        mlsClient.acceptLatestCommit(groupIdBase64);
        String welcomeBase64 = new String(Base64.getEncoder().encode(welcome));
        mlsClient2.processWelcomeMessage(welcomeBase64);
        assert mlsClient2.conversationExists(groupIdBase64);

        // Encrypt a message for the joined conversation
        String plainMessage = UUID.randomUUID().toString();
        final byte[] encryptedMessage = mlsClient.encrypt(groupIdBase64, plainMessage.getBytes());
        assert encryptedMessage.length > 10;
        final String encryptedBase64Message = Base64.getEncoder().encodeToString(encryptedMessage);

        final byte[] decrypted = mlsClient2.decrypt(groupIdBase64, encryptedBase64Message);
        assert new String(decrypted).equals(plainMessage);
    }

    @Test
    public void testMlsClientInitializationAndWipe() {
        // given
        QualifiedId user = new QualifiedId(UUID.randomUUID(), "wire.com");
        String client = "wipe_" + UUID.randomUUID();
        CryptoMlsClient cryptoMlsClient = new CryptoMlsClient(client, user, "pwd");
        assert cryptoMlsClient != null;

        Path path = Paths.get("mls/" + client);
        boolean pathExists = Files.exists(path);
        assert pathExists;

        // when
        cryptoMlsClient.wipe();

        // then
        assert Files.notExists(path);
    }
}
