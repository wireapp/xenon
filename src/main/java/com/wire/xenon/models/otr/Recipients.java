package com.wire.xenon.models.otr;

import com.wire.xenon.backend.models.QualifiedId;

import java.util.HashMap;
import java.util.Set;

/**
 * Used as request body when discovering client with or without existing crypto sessions.
 */
//<Qualified, ClientCipher> //Base64 encoded cipher
public class Recipients extends HashMap<QualifiedId, ClientCipher> {

    public String get(QualifiedId userId, String clientId) {
        HashMap<String, String> clients = toClients(userId);
        return clients.get(clientId);
    }

    public void add(QualifiedId userId, String clientId, String cipher) {
        ClientCipher clients = toClients(userId);
        clients.put(clientId, cipher);
    }

    //<UserId, <ClientId, Cipher>>
    public void add(QualifiedId userId, ClientCipher clients) {
        Set<String> clientIds = clients.keySet();
        for (String clientId : clientIds) {
            String bytes = clients.get(clientId);
            add(userId, clientId, bytes);
        }
    }

    public void add(Recipients recipients) {
        Set<QualifiedId> userIds = recipients.keySet();
        for (QualifiedId userId : userIds) {
            ClientCipher hashMap = recipients.get(userId);
            add(userId, hashMap);
        }
    }

    private ClientCipher toClients(QualifiedId userId) {
        return computeIfAbsent(userId, k -> new ClientCipher());
    }

}
