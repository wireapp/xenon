package com.wire.xenon.models.otr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wire.xenon.backend.models.QualifiedId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Collection of devices (clients) grouped by owning users. Users are also grouped by backend domain.
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Missing extends ConcurrentHashMap<String, ConcurrentHashMap<UUID, Collection<String>>> {
    public Collection<String> toClients(QualifiedId userId) {
        return get(userId.domain).get(userId.id);
    }

    public Collection<QualifiedId> toUserIds() {
        return entrySet().stream()
            .flatMap(
                u -> u.getValue()
                    .keySet()
                    .stream()
                    .map(strings -> new QualifiedId(strings, u.getKey()))
            )
            .collect(Collectors.toList());
    }

    public void add(QualifiedId userId, String clientId) {
        add(userId, List.of(clientId));
    }

    public void add(QualifiedId userId, Collection<String> clients) {
        Map<UUID, Collection<String>> userClientsMap = computeIfAbsent(userId.domain, k -> new ConcurrentHashMap<>());
        Collection<String> clientsList = userClientsMap.computeIfAbsent(userId.id, k -> new ArrayList<>());
        clientsList.addAll(clients);
    }
}
