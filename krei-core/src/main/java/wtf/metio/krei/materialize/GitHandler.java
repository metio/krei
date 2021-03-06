/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.vcs.git.Git;
import wtf.metio.krei.unit.git.GitUnits;

import java.nio.file.Path;
import java.util.function.Function;

public final class GitHandler implements Function<Git, Unit> {

    private final Path projectDirectory;

    GitHandler(final Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public Unit apply(final Git git) {
        final var builder = Unit.builder()
                .id("urn:krei:handler:git")
                .addBefore(GitUnits.initializeRepository(projectDirectory));
        git.remotes().stream().map(GitUnits::addGitRemote).forEach(builder::addRequires);
        return builder.build();
    }

}
