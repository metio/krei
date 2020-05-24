package wtf.metio.krei.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class TestResources {

    public static Path of(final String fileName, final Class<?> clazz) {
        return Paths.get("src/test/resources/",
                slashed(clazz.getPackageName()),
                slashed(clazz.getSimpleName()),
                fileName);
    }

    public static String read(final String fileName, final Class<?> clazz) throws IOException {
        return Files.readString(of(fileName, clazz), UTF_8);
    }

    public static String slashed(final String original) {
        return original.replace(".", "/") + "/";
    }

    private TestResources() {
        // factory class
    }

}
