package com.wire.xenon;

import com.google.protobuf.ByteString;
import com.waz.model.Messages;
import com.wire.xenon.backend.GenericMessageProcessor;
import com.wire.xenon.backend.models.QualifiedId;
import com.wire.xenon.models.AudioPreviewMessage;
import com.wire.xenon.models.LinkPreviewMessage;
import com.wire.xenon.models.MessageBase;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericMessageProcessorTest {

    public static final String AUDIO_MIME_TYPE = "audio/x-m4a";
    public static final String NAME = "audio.m4a";
    public static final int DURATION = 27000;
    private static final String TITLE = "title";
    private static final String SUMMARY = "summary";
    private static final String URL = "https://wire.com";
    private static final String CONTENT = "This is https://wire.com";
    private static final int URL_OFFSET = 8;
    private static final String ASSET_KEY = "key";
    private static final String ASSET_TOKEN = "token";
    private static final int HEIGHT = 43;
    private static final int WIDTH = 84;
    private static final int SIZE = 123;
    private static final String MIME_TYPE = "image/png";

    @Test
    public void testLinkPreview() {
        MessageHandler handler = new MessageHandler();
        GenericMessageProcessor processor = new GenericMessageProcessor(null, handler);

        UUID eventId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        QualifiedId from = new QualifiedId(UUID.randomUUID(), UUID.randomUUID().toString());
        QualifiedId convId = new QualifiedId(UUID.randomUUID(), UUID.randomUUID().toString());
        String sender = UUID.randomUUID().toString();
        String time = new Date().toString();

        Messages.Asset.ImageMetaData.Builder image = Messages.Asset.ImageMetaData.newBuilder()
                .setHeight(HEIGHT)
                .setWidth(WIDTH);

        Messages.Asset.Original.Builder original = Messages.Asset.Original.newBuilder()
                .setSize(SIZE)
                .setMimeType(MIME_TYPE)
                .setImage(image);

        Messages.Asset.RemoteData.Builder uploaded = Messages.Asset.RemoteData.newBuilder()
                .setAssetId(ASSET_KEY)
                .setAssetToken(ASSET_TOKEN)
                .setOtrKey(ByteString.EMPTY)
                .setSha256(ByteString.EMPTY);

        Messages.Asset.Builder asset = Messages.Asset.newBuilder()
                .setOriginal(original)
                .setUploaded(uploaded);

        Messages.LinkPreview.Builder linkPreview = Messages.LinkPreview.newBuilder()
                .setTitle(TITLE)
                .setSummary(SUMMARY)
                .setUrl(URL)
                .setUrlOffset(URL_OFFSET)
                .setImage(asset);

        Messages.Text.Builder text = Messages.Text.newBuilder()
                .setContent(CONTENT)
                .addLinkPreview(linkPreview);

        Messages.GenericMessage.Builder builder = Messages.GenericMessage.newBuilder()
                .setMessageId(messageId.toString())
                .setText(text);

        MessageBase msgBase = new MessageBase(eventId, messageId, convId, sender, from, time);
        processor.process(msgBase, builder.build());
    }

    @Test
    public void testAudioOrigin() {
        MessageHandler handler = new MessageHandler();
        GenericMessageProcessor processor = new GenericMessageProcessor(null, handler);

        UUID eventId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        QualifiedId from = new QualifiedId(UUID.randomUUID(), UUID.randomUUID().toString());
        QualifiedId convId = new QualifiedId(UUID.randomUUID(), UUID.randomUUID().toString());
        String sender = UUID.randomUUID().toString();
        String time = new Date().toString();
        byte[] levels = new byte[100];
        new Random().nextBytes(levels);

        Messages.Asset.AudioMetaData.Builder audioMeta = Messages.Asset.AudioMetaData.newBuilder()
                .setDurationInMillis(DURATION)
                .setNormalizedLoudness(ByteString.copyFrom(levels));

        Messages.Asset.Original.Builder original = Messages.Asset.Original.newBuilder()
                .setSize(SIZE)
                .setName(NAME)
                .setMimeType(AUDIO_MIME_TYPE)
                .setAudio(audioMeta);

        Messages.Asset.Builder asset = Messages.Asset.newBuilder()
                .setOriginal(original);

        Messages.GenericMessage.Builder builder = Messages.GenericMessage.newBuilder()
                .setMessageId(messageId.toString())
                .setAsset(asset);

        MessageBase msgBase = new MessageBase(eventId, messageId, convId, sender, from, time);
        processor.process(msgBase, builder.build());
    }

    @Test
    public void testAudioUploaded() {
        MessageHandler handler = new MessageHandler();
        GenericMessageProcessor processor = new GenericMessageProcessor(null, handler);

        UUID eventId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        QualifiedId from = new QualifiedId(UUID.randomUUID(), UUID.randomUUID().toString());
        QualifiedId convId = new QualifiedId(UUID.randomUUID(), UUID.randomUUID().toString());
        String sender = UUID.randomUUID().toString();
        String time = new Date().toString();
        byte[] levels = new byte[100];
        new Random().nextBytes(levels);

        Messages.Asset.RemoteData.Builder uploaded = Messages.Asset.RemoteData.newBuilder()
                .setAssetId(ASSET_KEY)
                .setAssetToken(ASSET_TOKEN)
                .setOtrKey(ByteString.EMPTY)
                .setSha256(ByteString.EMPTY);

        Messages.Asset.Builder asset = Messages.Asset.newBuilder()
                .setUploaded(uploaded);

        Messages.GenericMessage.Builder builder = Messages.GenericMessage.newBuilder()
                .setMessageId(messageId.toString())
                .setAsset(asset);

        MessageBase msgBase = new MessageBase(eventId, messageId, convId, sender, from, time);
        processor.process(msgBase, builder.build());
    }

    private static class MessageHandler extends MessageHandlerBase {
        @Override
        public void onLinkPreview(WireClient client, LinkPreviewMessage msg) {
            assertEquals(TITLE, msg.getTitle());
            assertEquals(SUMMARY, msg.getSummary());
            assertEquals(URL, msg.getUrl());
            assertEquals(URL_OFFSET, msg.getUrlOffset());
            assertEquals(CONTENT, msg.getText());
            assertEquals(SIZE, msg.getSize());
            assertEquals(MIME_TYPE, msg.getMimeType());
        }

        @Override
        public void onAudioPreview(WireClient client, AudioPreviewMessage msg) {
            assertEquals(AUDIO_MIME_TYPE, msg.getMimeType());
        }
    }
}
