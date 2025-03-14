package com.wire.xenon;

import com.wire.xenon.assets.IAsset;
import com.wire.xenon.backend.KeyPackageUpdate;
import com.wire.xenon.backend.models.*;
import com.wire.xenon.exceptions.HttpException;
import com.wire.xenon.models.AssetKey;
import com.wire.xenon.models.otr.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WireAPI {
    Devices sendMessage(OtrMessage msg, boolean ignoreMissing) throws HttpException;

    Devices sendPartialMessage(OtrMessage msg, QualifiedId userId) throws HttpException;

    Collection<User> getUsers(Collection<QualifiedId> ids);

    User getSelf();

    Conversation getConversation();

    PreKeys getPreKeys(Missing missing);

    ArrayList<Integer> getAvailablePrekeys(@NotNull String client);

    void uploadPreKeys(ArrayList<PreKey> preKeys) throws IOException;

    AssetKey uploadAsset(IAsset asset) throws Exception;

    byte[] downloadAsset(String assetId, String domain, String assetToken) throws HttpException;

    boolean deleteConversation(UUID teamId) throws HttpException;

    void addService(UUID serviceId, UUID providerId) throws HttpException;

    void addParticipants(QualifiedId... userIds) throws HttpException;

    Conversation createConversation(String name, UUID teamId, List<QualifiedId> users) throws HttpException;

    Conversation createOne2One(UUID teamId, QualifiedId userId) throws HttpException;

    void leaveConversation(QualifiedId user) throws HttpException;

    User getUser(QualifiedId userId) throws HttpException;

    boolean hasDevice(QualifiedId userId, String clientId);

    void acceptConnection(QualifiedId user) throws Exception;

    FeatureConfig getFeatureConfig();

    void uploadClientPublicKey(String clientId, ClientUpdate clientUpdate);

    void uploadClientKeyPackages(String clientId, KeyPackageUpdate keyPackageUpdate);

    byte[] getConversationGroupInfo(QualifiedId conversationId);

    void commitMlsBundle(byte[] commitBundle);

    List<Conversation> getUserConversations();
}
