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
