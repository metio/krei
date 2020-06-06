/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.test;

import org.junit.jupiter.api.function.Executable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public final class FileAsserts {

    public static void assertFile(final Path file) {
        assertAll(
                fileExists(file),
                () -> assertTrue(Files.isRegularFile(file), "file is not actually a file"),
                () -> assertTrue(Files.isReadable(file), "file cannot be read")
        );
    }

    public static void assertFileContent(final Path file, final String content) {
        assertAll(
                () -> assertEquals(content, Files.readString(file, UTF_8), "file content does not match")
        );
    }

    public static void assertFiles(final Path... files) {
        assertAll(Arrays.stream(files)
                .map(FileAsserts::fileExists)
                .collect(Collectors.toList())
        );
    }

    private static Executable fileExists(final Path path) {
        return () -> assertTrue(Files.exists(path), String.format("Path [%s] does not exist", path));
    }

    private FileAsserts() {
        // factory class
    }

}
