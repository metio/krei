/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.git;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.test.FileAsserts;
import wtf.metio.krei.test.TestRunner;

import java.nio.file.Path;

class GitActionsTest {

    @Test
    void shouldCreateGitRepository(@TempDir final Path projectDirectory) {
        // given
        final var action = GitActions.initializeRepository(projectDirectory);

        // when
        TestRunner.assertAction(action);

        // then
        FileAsserts.assertFiles(projectDirectory, projectDirectory.resolve(".git"));
    }

}
