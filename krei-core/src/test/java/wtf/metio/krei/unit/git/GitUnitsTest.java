/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.git;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.test.FileAsserts;
import wtf.metio.krei.test.TestRunner;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GitUnitsTest {

    @Test
    void shouldCreateContributingGuide(@TempDir final Path projectDirectory) {
        // given
        // when
        // then
        assertNotNull(GitUnits.initializeRepository(projectDirectory));
    }

    @Test
    void shouldCreateGitRepository(@TempDir final Path projectDirectory) {
        // given
        final var unit = GitUnits.initializeRepository(projectDirectory);

        // when
        TestRunner.assertUnit(unit);

        // then
        FileAsserts.assertFiles(projectDirectory, projectDirectory.resolve(".git"));
    }

}
