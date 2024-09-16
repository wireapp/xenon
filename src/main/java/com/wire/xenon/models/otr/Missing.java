package com.wire.xenon.models.otr;

import com.wire.xenon.backend.models.Qualified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Structure to handle users and devices (clients) belonging to them.
 *
 * <p>
 *     Usually holds the users and their devices when a message needs to be sent.
 *     The sender will need to fetch all the devices that need to get notified inside a conversation,
 *     and encrypt the message for each of them.
 * </p>
 */
public class Missing extends ConcurrentHashMap<Qualified, Collection<String>> {
    public Collection<String> toClients(Qualified userId) {
        return get(userId);
    }

    public Collection<Qualified> toUserIds() {
        return keySet();
    }

    public void add(Qualified userId, String clientId) {
        Collection<String> clients = computeIfAbsent(userId, k -> new ArrayList<>());
        clients.add(clientId);
    }

    public void add(Qualified userId, Collection<String> clients) {
        Collection<String> old = computeIfAbsent(userId, k -> new ArrayList<>());
        old.addAll(clients);
    }
}
