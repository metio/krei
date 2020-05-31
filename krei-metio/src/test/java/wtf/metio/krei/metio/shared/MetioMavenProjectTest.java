/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.metio.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetioMavenProjectTest {

    @Test
    void shouldCreateMavenProject() {
        // given
        final var projectName = "test";

        // when
        final var project = MetioMavenProject.configure(projectName);

        // then
        assertNotNull(project);
    }

}
