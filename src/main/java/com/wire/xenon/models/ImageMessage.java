//
// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.xenon.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.waz.model.Messages;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated
public class ImageMessage extends MessageAssetBase {
    @JsonProperty
    private int height;
    @JsonProperty
    private int width;

    @JsonCreator
    public ImageMessage(@JsonProperty("eventId") UUID eventId,
                        @JsonProperty("messageId") UUID messageId,
                        @JsonProperty("conversationId") UUID convId,
                        @JsonProperty("clientId") String clientId,
                        @JsonProperty("userId") UUID userId,
                        @JsonProperty("time") String time,
                        @JsonProperty("assetKey") String assetKey,
                        @JsonProperty("assetToken") String assetToken,
                        @JsonProperty("otrKey") byte[] otrKey,
                        @JsonProperty("mimeType") String mimeType,
                        @JsonProperty("size") long size,
                        @JsonProperty("sha256") byte[] sha256,
                        @JsonProperty("name") String name) {
        super(eventId, messageId, convId, clientId, userId, time, assetKey, assetToken, otrKey, mimeType, size, sha256, name);
    }

    public ImageMessage(MessageAssetBase base, Messages.Asset.ImageMetaData image) {
        super(base);
        setHeight(image.getHeight());
        setWidth(image.getWidth());
    }

    public ImageMessage(UUID eventId, UUID msgId, UUID convId, String clientId, UUID userId, String time) {
        super(eventId, msgId, convId, clientId, userId, time);
    }

    public ImageMessage(MessageBase msg) {
        super(msg);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
