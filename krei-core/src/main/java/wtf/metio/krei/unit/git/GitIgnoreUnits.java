/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.git;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.task.file.FileTasks;

import java.nio.file.Path;

import static wtf.metio.krei.template.GitIgnoreTemplates.BAZEL;
import static wtf.metio.krei.template.GitIgnoreTemplates.MAVEN;

public final class GitIgnoreUnits {

    public static Unit ignoreMavenOutput(final Path gitignoreFile) {
        return Unit.builder()
                .id("urn:krei:git:ignore:maven-output")
                .task(FileTasks.appendFile(gitignoreFile, MAVEN.ignores()))
                .build();
    }

    public static Unit ignoreBazelDirectories(final Path gitignoreFile) {
        return Unit.builder()
                .id("urn:krei:git:ignore:bazel-directories")
                .task(FileTasks.appendFile(gitignoreFile, BAZEL.ignores()))
                .build();
    }

    private GitIgnoreUnits() {
        // factory class
    }

}
