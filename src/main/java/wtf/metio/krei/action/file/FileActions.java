package wtf.metio.krei.action.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.metio.krei.model.Action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public final class FileActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileActions.class);

    public static Action appendFile(final Path file, final String content) {
        return append(() -> Files.writeString(file, content, CREATE, APPEND));
    }

    static Action append(final Callable<Path> callable) {
        return () -> {
            try {
                callable.call();
                return 0;
            } catch (final Exception exception) {
                LOGGER.error("Could not append to file", exception);
                return 1000;
            }
        };
    }

    private FileActions() {
        // factory class
    }

}
