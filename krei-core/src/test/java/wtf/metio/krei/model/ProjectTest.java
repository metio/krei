/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.unit.file.FileUnits;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void shouldCreateProject(@TempDir final Path projectDirectory) {
        assertNotNull(Project.builder(projectDirectory).build());
    }

    @Test
    void shouldCreateProjectWithUnits(@TempDir final Path projectDirectory) {
        assertNotNull(Project.builder(projectDirectory).addUnits(FileUnits.createDirectory(projectDirectory)).build());
    }

}
