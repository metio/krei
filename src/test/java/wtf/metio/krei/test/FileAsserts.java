package wtf.metio.krei.test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class FileAsserts {

    public static void assertFile(final Path file) {
        assertAll(
                () -> assertTrue(Files.exists(file), "file was not written"),
                () -> assertTrue(Files.isRegularFile(file), "file is not actually a file"),
                () -> assertTrue(Files.isReadable(file), "file cannot be read")
        );
    }

    private FileAsserts() {
        // factory class
    }

}
