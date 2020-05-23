package wtf.metio.krei.construct.metio;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.model.Configuration;
import wtf.metio.krei.usecase.DryRun;

import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static wtf.metio.krei.test.PrintStreams.withPrintStream;

class MetioMavenProjectTest {

    @Test
    void shouldCreateMavenProject(@TempDir final Path projectDirectory) {
        final var projectName = "test";
        final var config = new Configuration(projectDirectory, projectName);
        withPrintStream((ps, baos) -> {
            DryRun.execute(MetioMavenProject.create(config), ps);
            Assertions.assertNotNull(baos.toString(UTF_8));
        });
    }

}
