package com.wire.xenon;

import com.google.protobuf.InvalidProtocolBufferException;
import com.waz.model.Messages;
import com.wire.bots.cryptobox.CryptoException;
import com.wire.xenon.backend.GenericMessageProcessor;
import com.wire.xenon.backend.models.*;
import com.wire.xenon.models.MessageBase;
import com.wire.xenon.tools.Logger;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public abstract class MessageResourceBase {
    protected final MessageHandlerBase handler;
    protected static final Integer PREKEYS_DEFAULT_REPLENISH = 10;

    public MessageResourceBase(MessageHandlerBase handler) {
        this.handler = handler;
    }

    protected void handleMessage(UUID eventId, Payload payload, WireClient client) throws Exception {
        Payload.Data data = payload.data;
        UUID botId = client.getId();

        switch (payload.type) {
            case "conversation.otr-message-add":
                QualifiedId from = payload.from;

                Logger.info("conversation.otr-message-add: bot: %s from: %s:%s", botId, from, data.sender);

                GenericMessageProcessor processor = new GenericMessageProcessor(client, handler);

                Messages.GenericMessage genericMessage = decryptProteus(client, payload);

                final UUID messageId = UUID.fromString(genericMessage.getMessageId());
                MessageBase msgBase = new MessageBase(eventId, messageId, payload.conversation, data.sender, from, payload.time);

                processor.process(msgBase, genericMessage);

                handler.onEvent(client, from, genericMessage);
                break;
            case "conversation.mls-message-add":
                QualifiedId fromMls = payload.from;

                Logger.info("conversation.mls-message-add: bot: %s from: %s:%s", botId, fromMls, data.sender);

                GenericMessageProcessor processorMls = new GenericMessageProcessor(client, handler);

                Messages.GenericMessage genericMessageMls = decryptMls(client, payload);

                final UUID messageIdMls = UUID.fromString(genericMessageMls.getMessageId());
                MessageBase msgBaseMls = new MessageBase(eventId, messageIdMls, payload.conversation, data.sender, fromMls, payload.time);

                processorMls.process(msgBaseMls, genericMessageMls);

                handler.onEvent(client, fromMls, genericMessageMls);
                break;
            case "conversation.mls-welcome":
                Logger.info("conversation.mls-welcome: bot: %s in: %s", botId, payload.conversation);

                client.processWelcomeMessage(payload.data.text);

                SystemMessage welcomeSystemMessage = getSystemMessage(eventId, payload);
                handler.onNewConversation(client, welcomeSystemMessage);
                break;
            case "conversation.member-join":
                Logger.debug("conversation.member-join: bot: %s", botId);

                // Check if this bot got added to the conversation
                List<QualifiedId> participants = data.userIds;
                if (participants.remove(botId)) {
                    SystemMessage systemMessage = getSystemMessage(eventId, payload);
                    systemMessage.conversation = client.getConversation();
                    systemMessage.type = "conversation.create"; //hack the type

                    handler.onNewConversation(client, systemMessage);
                    return;
                }

                // Check if we still have some prekeys and keyPackages available. Upload them if needed
                handler.validatePreKeys(client, participants.size());
                client.checkAndReplenishKeyPackages();

                SystemMessage systemMessage = getSystemMessage(eventId, payload);
                systemMessage.users = data.userIds;

                handler.onMemberJoin(client, systemMessage);
                break;
            case "conversation.member-leave":
                Logger.debug("conversation.member-leave: bot: %s", botId);

                systemMessage = getSystemMessage(eventId, payload);
                systemMessage.users = data.userIds;

                // Check if this bot got removed from the conversation
                participants = data.userIds;
                if (participants.remove(botId)) {
                    handler.onBotRemoved(botId, systemMessage);
                    return;
                }

                if (!participants.isEmpty()) {
                    handler.onMemberLeave(client, systemMessage);
                }
                break;
            case "conversation.delete":
                Logger.debug("conversation.delete: bot: %s", botId);
                systemMessage = getSystemMessage(eventId, payload);

                // Cleanup
                handler.onConversationDelete(botId, systemMessage);
                break;
            case "conversation.create":
                Logger.debug("conversation.create: bot: %s", botId);

                systemMessage = getSystemMessage(eventId, payload);
                Integer otherMembers = PREKEYS_DEFAULT_REPLENISH;
                if (systemMessage.conversation.members != null) {
                    otherMembers = systemMessage.conversation.members.others.size();
                    Member self = new Member();
                    String selfDomain = null;
                    if (systemMessage.conversation.id != null) {
                        selfDomain = systemMessage.conversation.id.domain;
                    }
                    self.id = new QualifiedId(botId, selfDomain);
                    systemMessage.conversation.members.others.add(self);
                }

                // Check if we still have some prekeys and keyPackages available. Upload them if needed
                handler.validatePreKeys(client, otherMembers);
                client.checkAndReplenishKeyPackages();

                handler.onNewConversation(client, systemMessage);
                break;
            case "conversation.rename":
                Logger.debug("conversation.rename: bot: %s", botId);

                systemMessage = getSystemMessage(eventId, payload);

                handler.onConversationRename(client, systemMessage);
                break;
            case "user.connection":
                Payload.Connection connection = payload.connection;
                Logger.debug("user.connection: bot: %s, from: %s to: %s status: %s",
                        botId,
                        connection.from,
                        connection.to,
                        connection.status);

                boolean accepted = handler.onConnectRequest(client, connection.from, connection.to, connection.status);
                if (accepted) {
                    Conversation conversation = new Conversation();
                    conversation.id = connection.conversation;
                    systemMessage = new SystemMessage();
                    systemMessage.id = eventId;
                    systemMessage.from = connection.from;
                    systemMessage.type = payload.type;
                    systemMessage.conversation = conversation;

                    handler.onNewConversation(client, systemMessage);
                }
                break;
            default:
                Logger.debug("Unknown event: %s", payload.type);
        }
    }

    private SystemMessage getSystemMessage(UUID eventId, Payload payload) {
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.id = eventId;
        systemMessage.from = payload.from;
        systemMessage.time = payload.time;
        systemMessage.type = payload.type;

        systemMessage.conversation = new Conversation();
        systemMessage.conversation.id = payload.conversation;

        if (payload.data != null) {
            systemMessage.conversation.creator = payload.data.creator;
            systemMessage.conversation.name = payload.data.name;
            if (payload.data.members != null)
                systemMessage.conversation.members = new Payload.Members();
                systemMessage.conversation.members.others = payload.data.members.others;
        }

        return systemMessage;
    }

    private Messages.GenericMessage decryptProteus(WireClient client, Payload payload)
            throws CryptoException, InvalidProtocolBufferException {
        QualifiedId from = payload.from;
        String sender = payload.data.sender;
        String cipher = payload.data.text;

        String encoded = client.decryptProteus(from, sender, cipher);
        byte[] decoded = Base64.getDecoder().decode(encoded);
        return Messages.GenericMessage.parseFrom(decoded);
    }

    private Messages.GenericMessage decryptMls(WireClient client, Payload payload)
        throws IOException {
        String mlsGroupId = client.getConversation().mlsGroupId;
        Logger.info("Fetched MLS group id: %s from conversation id: %s", mlsGroupId, payload.conversation);
        String cipher = payload.data.text;

        byte[] decoded = client.decryptMls(mlsGroupId, cipher);
        return Messages.GenericMessage.parseFrom(decoded);
    }
}
