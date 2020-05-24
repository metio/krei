package wtf.metio.krei.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class Templates {

    public static String template(final String category, final String file) {
        final var template = String.format("/template/%s/%s", category, file);
        try (final var is = Templates.class.getResourceAsStream(template);
             final var br = new BufferedReader(new InputStreamReader(is, UTF_8))) {
            final var sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            return sb.toString();
        } catch (final IOException exception) {
            exception.printStackTrace();
            return "";
        }
    }

    private Templates() {
        // factory class
    }

}
