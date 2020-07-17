/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.file;

import wtf.metio.krei.action.utils.Callables;
import wtf.metio.krei.model.Action;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public final class FileActions {

    public static Action createDirectory(final Path directory) {
        return Callables.call(() -> Files.createDirectories(directory.toAbsolutePath()));
    }

    public static Action move(final Path source, final Path target) {
        return Callables.call(() -> Files.move(source.toAbsolutePath(), target.toAbsolutePath()));
    }

    public static Action appendFile(final Path file, final String content) {
        return Callables.call(() -> Files.writeString(file, content, CREATE, APPEND));
    }

    private FileActions() {
        // factory class
    }

}
