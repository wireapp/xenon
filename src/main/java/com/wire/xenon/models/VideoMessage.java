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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.waz.model.Messages;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class VideoMessage extends MessageAssetBase {
    @JsonProperty
    private long duration;
    @JsonProperty
    private int width;
    @JsonProperty
    private int height;

    @JsonCreator
    public VideoMessage(@JsonProperty("eventId") UUID eventId,
                        @JsonProperty("messageId") UUID messageId,
                        @JsonProperty("conversationId") UUID convId,
                        @JsonProperty("clientId") String clientId,
                        @JsonProperty("userId") UUID userId,
                        @JsonProperty("time") String time) {
        super(eventId, messageId, convId, clientId, userId, time);
    }

    public VideoMessage(MessageAssetBase base, Messages.Asset.VideoMetaData video) {
        super(base);
        setDuration(video.getDurationInMillis());
        setHeight(video.getHeight());
        setWidth(video.getWidth());
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }
}
