package wtf.metio.krei.action.file;

import wtf.metio.krei.model.Action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public final class FileActions {

    public static Action appendFile(final Path file, final String content) {
        return () -> {
            try {
                Files.writeString(file, content, CREATE, APPEND);
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
            return 0;
        };
    }

    private FileActions() {
        // factory class
    }

}
