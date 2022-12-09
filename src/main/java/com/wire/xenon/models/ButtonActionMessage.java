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

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ButtonActionMessage extends MessageBase {
    @JsonProperty
    private String buttonId;

    @JsonProperty
    private UUID reference;

    @JsonCreator
    public ButtonActionMessage(@JsonProperty("eventId") UUID eventId,
                               @JsonProperty("messageId") UUID messageId,
                               @JsonProperty("conversationId") UUID convId,
                               @JsonProperty("clientId") String clientId,
                               @JsonProperty("userId") UUID userId,
                               @JsonProperty("time") String time) {
        super(eventId, messageId, convId, clientId, userId, time);
    }

    public ButtonActionMessage(MessageBase msgBase) {
        super(msgBase);
    }

    public String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }

    public UUID getReference() {
        return reference;
    }

    public void setReference(UUID reference) {
        this.reference = reference;
    }
}
