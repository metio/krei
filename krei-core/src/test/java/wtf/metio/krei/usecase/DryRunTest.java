/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.usecase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.model.Project;
import wtf.metio.krei.unit.git.GitIgnoreUnits;

import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static wtf.metio.krei.test.PrintStreams.withPrintStream;

class DryRunTest {

    @Test
    void shouldAllowDryRun(@TempDir final Path baseDirectory) {
        // given
        final var projectName = "krei";
        final var projectDirectory = baseDirectory.resolve(projectName);
        final var gitignoreDirectory = baseDirectory.resolve(".gitignore");

        // when
        final var project = Project.builder(projectDirectory)
                .addUnits(GitIgnoreUnits.ignoreBazelDirectories(gitignoreDirectory))
                .build();

        // then
        withPrintStream((ps, baos) -> {
            DryRun.execute(project, ps);
            Assertions.assertNotNull(baos.toString(UTF_8));
        });
    }

}