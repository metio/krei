/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.model.ProjectConfig;
import wtf.metio.krei.model.vcs.git.Git;
import wtf.metio.krei.test.FileAsserts;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MaterializeTest {

    @Test
    void shouldMaterialize(@TempDir final Path projectDirectory) {
        // given
        final var config = ProjectConfig.builder("test").build();

        // when
        final var project = Materialize.asProject(config, projectDirectory);

        // then
        assertNotNull(project);
    }

    @Test
    void shouldMaterializeGitRepo(@TempDir final Path projectDirectory) {
        // given
        final var config = ProjectConfig.builder("test")
                .addVcs(Git.builder().build())
                .build();

        // when
        final var result = Materialize.intoFilesystem(config, projectDirectory);

        // then
        assertEquals(0, result);
        FileAsserts.assertFiles(projectDirectory, projectDirectory.resolve(".git"));
    }

}
