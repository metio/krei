/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.metio.krei.action.utils.Callables;
import wtf.metio.krei.model.Action;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public final class FileActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileActions.class);

    public static Action createDirectory(final Path directory) {
        return Callables.call(() -> Files.createDirectories(directory.toAbsolutePath()));
    }

    public static Action move(final Path source, final Path target) {
        return Callables.call(() -> Files.move(source.toAbsolutePath(), target.toAbsolutePath()));
    }

    public static Action appendFile(final Path file, final String content) {
        return Callables.call(() -> Files.writeString(file, content, CREATE, APPEND));
    }

    static Action append(final Callable<Path> callable) {
        return () -> {
            try {
                callable.call();
                return 0;
            } catch (final Exception exception) {
                LOGGER.error("Could not append to file", exception);
                return 1000;
            }
        };
    }

    private FileActions() {
        // factory class
    }

}
