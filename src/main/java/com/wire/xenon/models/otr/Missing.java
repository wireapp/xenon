package com.wire.xenon.models.otr;

import com.wire.xenon.backend.models.QualifiedId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collection of devices (clients) grouped by owning users.
 *
 * <p>
 *     Holds the users and their devices when a message needs to be sent.
 *     The sender will need to fetch all the devices that need to get notified inside a conversation,
 *     and encrypt the message for each of them. Encryption can be done directly if a crypto session
 *     has already been established for a device, or prekeys will need to be fetched from the backend.
 *     NOTE: Name might be misleading, it comes from trying to send a message in a conversation without specifying
 *     any clientId, where the backend will respond with the clientId "missing" from the request.
 * </p>
 */
public class Missing extends ConcurrentHashMap<QualifiedId, Collection<String>> {
    public Collection<String> toClients(QualifiedId userId) {
        return get(userId);
    }

    public Collection<QualifiedId> toUserIds() {
        return keySet();
    }

    public void add(QualifiedId userId, String clientId) {
        Collection<String> clients = computeIfAbsent(userId, k -> new ArrayList<>());
        clients.add(clientId);
    }

    public void add(QualifiedId userId, Collection<String> clients) {
        Collection<String> old = computeIfAbsent(userId, k -> new ArrayList<>());
        old.addAll(clients);
    }
}
