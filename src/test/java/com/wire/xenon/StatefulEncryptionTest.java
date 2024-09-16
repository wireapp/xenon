package com.wire.xenon;

import com.google.protobuf.InvalidProtocolBufferException;
import com.waz.model.Messages;
import com.wire.bots.cryptobox.IStorage;
import com.wire.xenon.assets.MessageText;
import com.wire.xenon.backend.models.Qualified;
import com.wire.xenon.crypto.CryptoDatabase;
import com.wire.xenon.crypto.storage.JdbiStorage;
import com.wire.xenon.models.otr.PreKey;
import com.wire.xenon.models.otr.PreKeys;
import com.wire.xenon.models.otr.Recipients;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class StatefulEncryptionTest extends DatabaseTestBase {
    @Test
    public void testAliceToAlice() throws Exception {
        Qualified aliceId = new Qualified(UUID.randomUUID(), UUID.randomUUID().toString());
        String client1 = "alice1_" + UUID.randomUUID();
        String rootFolder = "xenon-unit-test-" + UUID.randomUUID();
        IStorage storage = new JdbiStorage(jdbi);

        // Setup crypto sessions for Alice
        CryptoDatabase aliceCrypto = new CryptoDatabase(aliceId, storage, rootFolder + "/testAliceToAlice/1");
        CryptoDatabase aliceCrypto1 = new CryptoDatabase(aliceId, storage, rootFolder + "/testAliceToAlice/2");

        // Generate prekeys
        final PreKey lastPreKey = aliceCrypto.newLastPreKey();
        final PreKey lastPreKey1 = aliceCrypto1.newLastPreKey();
        assert lastPreKey != null;
        assert lastPreKey1 != null;

        final ArrayList<PreKey> alicePreKeys = aliceCrypto.newPreKeys(0, 50);
        final ArrayList<PreKey> alice1PreKeys = aliceCrypto1.newPreKeys(0, 50);

        // Encrypt simple message for Alice client with prekeys
        String text = "Hello Alice, This is Alice!";
        final MessageText messageText = new MessageText(text);
        PreKeys alice1SendingPrekeys = new PreKeys(alice1PreKeys, client1, aliceId);
        assert alice1SendingPrekeys.get(aliceId).size() == 1;
        final Recipients encrypted = aliceCrypto.encrypt(alice1SendingPrekeys, messageText.createGenericMsg().toByteArray());
        assert encrypted.size() == 1;

        String decrypt = aliceCrypto1.decrypt(aliceId, client1, encrypted.get(aliceId, client1));
        String decryptedText = getText(decrypt);
        assert text.equals(decryptedText);

    }

    private String getText(String decrypt) throws InvalidProtocolBufferException {
        byte[] decoded = Base64.getDecoder().decode(decrypt);
        Messages.GenericMessage genericMessage = Messages.GenericMessage.parseFrom(decoded);
        return genericMessage.getText().getContent();
    }
}
