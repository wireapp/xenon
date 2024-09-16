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

import com.waz.model.Messages;

public class AudioPreviewMessage extends OriginMessage {
    private long duration;
    private byte[] levels;

    public AudioPreviewMessage(MessageBase msg, Messages.Asset.Original original) {
        super(msg);

        setMimeType(original.getMimeType());
        setSize(original.getSize());
        setName(original.getName());
        setDuration(original.getAudio().getDurationInMillis());
        setLevels(original.getAudio().getNormalizedLoudness().toByteArray());
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public byte[] getLevels() {
        return levels;
    }

    public void setLevels(byte[] levels) {
        this.levels = levels;
    }
}
