package wtf.metio.krei.unit.idea;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.metio.krei.unit.git.GitUnits;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class IdeaUnitsTest {

    @Test
    void shouldCreateContributingGuide(@TempDir final Path projectDirectory) {
        assertNotNull(IdeaUnits.configureGitignore(projectDirectory.resolve(".idea")));
    }

}
