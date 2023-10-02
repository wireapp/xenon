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

package com.wire.xenon.assets;

import com.waz.model.Messages;
import com.wire.xenon.tools.Logger;

import java.util.UUID;

public class LinkPreview implements IGeneric {
    private final String url;
    private final String title;
    private final ImageAsset thumbnail;
    private final UUID messageId = UUID.randomUUID();

    public LinkPreview(String url, String title, ImageAsset thumbnail) {
        this.url = url;
        this.title = title;
        this.thumbnail = thumbnail;
    }

    @Override
    public Messages.GenericMessage createGenericMsg() {
        Messages.Asset preview = null;
        try {
            preview = thumbnail.createGenericMsg().getAsset();
        } catch (Exception e) {
            Logger.warning("LinkPreview: %s", e);
        }

        // Legacy todo: remove it!
        Messages.Article article = Messages.Article.newBuilder()
                .setTitle(title)
                .setPermanentUrl(url)
                .setImage(preview)
                .build();
        // Legacy

        Messages.LinkPreview.Builder linkPreview = Messages.LinkPreview.newBuilder()
                .setUrl(url)
                .setUrlOffset(0)
                .setImage(preview)
                .setPermanentUrl(url)
                .setTitle(title)
                .setArticle(article);

        Messages.Text.Builder text = Messages.Text.newBuilder()
                .setContent(url)
                .addLinkPreview(linkPreview);

        return Messages.GenericMessage.newBuilder()
                .setMessageId(getMessageId().toString())
                .setText(text.build())
                .build();
    }

    @Override
    public UUID getMessageId() {
        return messageId;
    }
}
