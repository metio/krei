/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.test;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public final class FileAsserts {

    public static void assertFile(final Path file) {
        assertAll(
                () -> assertTrue(Files.exists(file), "file was not written"),
                () -> assertTrue(Files.isRegularFile(file), "file is not actually a file"),
                () -> assertTrue(Files.isReadable(file), "file cannot be read")
        );
    }

    public static void assertFileContent(final Path file, final String content) {
        assertAll(
                () -> assertEquals(content, Files.readString(file, UTF_8), "file content does not match")
        );
    }

    private FileAsserts() {
        // factory class
    }

}
