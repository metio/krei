/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.file;

import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

public final class FileUnits {

    public static Unit createDirectory(final Path directory) {
        return Unit.builder()
                .id("urn:krei:file:directory:create")
                .action(FileActions.createDirectory(directory))
                .build();
    }

    public static Unit move(final Path source, final Path target) {
        return Unit.builder()
                .id("urn:krei:file:move:" + source.toAbsolutePath().toString())
                .action(FileActions.move(source, target))
                .build();
    }

    public static Unit appendFile(final Path path, final String content) {
        return Unit.builder()
                .id("urn:krei:file:append:" + content.hashCode())
                .action(FileActions.appendFile(path, content))
                .build();
    }

    private FileUnits() {
        // factory class
    }

}
