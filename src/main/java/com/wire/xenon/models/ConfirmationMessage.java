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

import java.util.UUID;

public class ConfirmationMessage extends MessageBase {
    private Type type;
    private UUID confirmationMessageId;

    public ConfirmationMessage(MessageBase msg) {
        super(msg);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public UUID getConfirmationMessageId() {
        return confirmationMessageId;
    }

    public void setConfirmationMessageId(UUID confirmationMessageId) {
        this.confirmationMessageId = confirmationMessageId;
    }

    public enum Type {
        DELIVERED,
        READ
    }
}
