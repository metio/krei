package wtf.metio.krei.materialize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.model.ProjectConfig;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MaterializeTest {

    @Test
    void shouldMaterialize(@TempDir final Path projectDirectory) {
        // given
        final var config = ProjectConfig.builder("test").build();

        // when
        final var project = Materialize.into(projectDirectory).asProject(config);

        // then
        assertNotNull(project);
    }

}
