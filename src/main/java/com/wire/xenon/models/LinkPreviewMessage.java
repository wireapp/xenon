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
import com.wire.xenon.backend.models.QualifiedId;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkPreviewMessage extends OriginMessage {
    @JsonProperty
    private String summary;
    @JsonProperty
    private String title;
    @JsonProperty
    private String url;
    @JsonProperty
    private String text;
    @JsonProperty
    private int urlOffset;

    @JsonCreator
    public LinkPreviewMessage(@JsonProperty("eventId") UUID eventId,
                              @JsonProperty("messageId") UUID messageId,
                              @JsonProperty("conversationId") QualifiedId convId,
                              @JsonProperty("clientId") String clientId,
                              @JsonProperty("userId") QualifiedId userId,
                              @JsonProperty("time") String time) {
        super(eventId, messageId, convId, clientId, userId, time);
    }

    public LinkPreviewMessage(MessageBase msg) {
        super(msg);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getUrlOffset() {
        return urlOffset;
    }

    public void setUrlOffset(int urlOffset) {
        this.urlOffset = urlOffset;
    }
}
