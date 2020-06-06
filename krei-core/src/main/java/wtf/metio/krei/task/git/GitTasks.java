/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.task.git;

import wtf.metio.krei.action.git.GitActions;
import wtf.metio.krei.model.Task;

import java.nio.file.Path;

public final class GitTasks {

    public static Task initializeRepository(final Path projectDirectory) {
        return Task.builder()
                .action(GitActions.initializeRepository(projectDirectory))
                .build();
    }

    private GitTasks() {
        // factory class
    }

}
