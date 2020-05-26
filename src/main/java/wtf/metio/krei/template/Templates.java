package wtf.metio.krei.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public final class Templates {

    private static final Logger LOGGER = LoggerFactory.getLogger(Templates.class);

    public static String template(final String category, final String file) {
        LOGGER.debug("reading [/template{}/{}]", category, file);
        final var template = String.format("/template/%s/%s", category, file);
        try (final var resource = Templates.class.getResourceAsStream(template);
             final var reader = new BufferedReader(new InputStreamReader(resource, UTF_8))) {
            final var content = reader.lines().collect(joining(lineSeparator(), "", lineSeparator()));
            LOGGER.trace("read [/template{}/{}]\n{}", category, file, content);
            return content;
        } catch (final IOException exception) {
            LOGGER.error("Could not read template", exception);
            return "";
        }
    }

    private Templates() {
        // factory class
    }

}
