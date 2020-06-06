/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.git;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.vcs.git.GitRemote;
import wtf.metio.krei.task.git.GitTasks;

import java.nio.file.Path;

public final class GitUnits {

    public static Unit initializeRepository(final Path projectDirectory) {
        return Unit.builder()
                .id("urn:krei:git:repo:init")
                .task(GitTasks.initializeRepository(projectDirectory))
                .build();
    }

    public static Unit addGitRemote(final GitRemote remote) {
        return Unit.builder()
                .id("urn:krei:git:remote:add:" + remote.name())
                .build();
    }

    public static Unit configureUserEmail(final Path path, final String email) {
        return Unit.builder()
                .id("urn:krei:git:config:user-email")
                // .addExec("git", "config", "--file", path.toAbsolutePath().toString(), "user.email", email)
                .build();
    }

    private GitUnits() {
        // factory class
    }

}
