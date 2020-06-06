/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.predicate.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathExistsTest {

    @Test
    void shouldDetectExistingPath(@TempDir final Path directory) throws IOException {
        // given
        final var check = new PathExists(directory);

        // when
        final var result = check.test();

        // then
        assertTrue(result);
    }

    @Test
    void shouldDetectNonExistingPath(@TempDir final Path directory) throws IOException {
        // given
        final var check = new PathExists(directory.resolve("does-not-exist"));

        // when
        final var result = check.test();

        // then
        assertFalse(result);
    }

}
