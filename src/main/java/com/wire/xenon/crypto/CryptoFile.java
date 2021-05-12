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

package com.wire.xenon.crypto;

import com.wire.bots.cryptobox.CryptoBox;
import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.cryptobox.ICryptobox;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

/**
 * Wrapper for the Crypto Box. This class is thread safe.
 */
public class CryptoFile extends CryptoBase {
    private final CryptoBox box;
    private final String root;

    /**
     * <p>
     * Opens the CryptoBox using given directory path
     * The given directory must be writable.
     * </p>
     * Note: Do not create multiple OtrManagers that operate on the same or
     * overlapping directories. Doing so results in undefined behaviour.
     *
     * @param rootDir The root storage directory of the box
     * @param botId   Bot id
     * @throws CryptoException when crypto breaks
     */
    public CryptoFile(String rootDir, UUID botId) throws CryptoException {
        root = String.format("%s/%s", rootDir, botId);
        box = CryptoBox.open(root);
    }

    @Override
    public ICryptobox box() {
        return box;
    }

    @Override
    public void purge() throws IOException {
        box.close();
        Path rootPath = Paths.get(root);
        if (!rootPath.toFile().exists()) return;

        // we don't really care if the files were actually deleted or not
        //noinspection ResultOfMethodCallIgnored
        Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
