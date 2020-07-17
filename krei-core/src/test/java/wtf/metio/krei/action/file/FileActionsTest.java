/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.test.FileAsserts;

import java.nio.file.Path;

import static wtf.metio.krei.test.FileAsserts.assertFile;

class FileActionsTest {

    @Test
    void shouldWriteFile(@TempDir final Path directory) {
        // given
        final var file = directory.resolve("test.txt");

        // when
        FileActions.appendFile(file, "hello world").call();

        // then
        assertFile(file);
    }

    @Test
    void shouldAppendToFile(@TempDir final Path directory) {
        // given
        final var file = directory.resolve("test.txt");

        // when
        FileActions.appendFile(file, "hello").call();
        FileActions.appendFile(file, "world").call();

        // then
        FileAsserts.assertFileContent(file, "helloworld");
    }

}
