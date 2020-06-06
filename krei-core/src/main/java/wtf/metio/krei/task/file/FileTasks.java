/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.task.file;

import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.model.Task;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.predicate.file.FilePredicates;

import java.nio.file.Path;

public final class FileTasks {

    public static Task createDirectory(final Path directory) {
        return Task.builder()
                .action(FileActions.createDirectory(directory))
                .check(FilePredicates.exists(directory))
                .build();
    }

    public static Task move(final Path source, final Path target) {
        return Task.builder()
                .action(FileActions.move(source, target))
                .build();
    }

    public static Task appendFile(final Path path, final String content) {
        return Task.builder()
                .action(FileActions.appendFile(path, content))
                .build();
    }

    private FileTasks() {
        // factory class
    }
    
}
