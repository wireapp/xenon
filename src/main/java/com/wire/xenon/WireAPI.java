package com.wire.xenon;

import com.wire.xenon.assets.IAsset;
import com.wire.xenon.backend.models.Conversation;
import com.wire.xenon.backend.models.Qualified;
import com.wire.xenon.backend.models.User;
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
    Devices sendMessage(OtrMessage msg, Object... ignoreMissing) throws HttpException;

    Devices sendPartialMessage(OtrMessage msg, Qualified userId) throws HttpException;

    Collection<User> getUsers(Collection<Qualified> ids);

    User getSelf();

    Conversation getConversation();

    PreKeys getPreKeys(Missing missing);

    ArrayList<Integer> getAvailablePrekeys(@NotNull String client);

    void uploadPreKeys(ArrayList<PreKey> preKeys) throws IOException;

    AssetKey uploadAsset(IAsset asset) throws Exception;

    byte[] downloadAsset(String assetId, String assetToken) throws HttpException;

    boolean deleteConversation(UUID teamId) throws HttpException;

    void addService(UUID serviceId, UUID providerId) throws HttpException;

    Conversation addParticipants(Qualified... userIds) throws HttpException;

    Conversation createConversation(String name, UUID teamId, List<Qualified> users) throws HttpException;

    Conversation createOne2One(UUID teamId, Qualified userId) throws HttpException;

    void leaveConversation(Qualified user) throws HttpException;

    User getUser(Qualified userId) throws HttpException;

    Qualified getUserId(String handle) throws HttpException;

    boolean hasDevice(Qualified userId, String clientId);

    UUID getTeam() throws HttpException;

    Collection<Qualified> getTeamMembers(UUID teamId);

    void acceptConnection(Qualified user) throws Exception;
}
