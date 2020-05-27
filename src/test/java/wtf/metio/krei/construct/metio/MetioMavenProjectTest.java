package wtf.metio.krei.construct.metio;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetioMavenProjectTest {

    @Test
    void shouldCreateMavenProject() {
        // given
        final var projectName = "test";

        // when
        final var project = MetioMavenProject.configure(projectName);

        // then
        Assertions.assertNotNull(project);
    }

}
