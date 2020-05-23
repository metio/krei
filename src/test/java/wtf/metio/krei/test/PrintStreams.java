package wtf.metio.krei.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PrintStreams {

    public static void withPrintStream(final BiConsumer<PrintStream, ByteArrayOutputStream> consumer) {
        final var baos = new ByteArrayOutputStream();
        try (final var ps = new PrintStream(baos, true, UTF_8)) {
            consumer.accept(ps, baos);
        }
    }

    private PrintStreams() {
        // factory class
    }

}
