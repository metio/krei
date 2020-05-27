package wtf.metio.krei.unit.git;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GitUnitsTest {

    @Test
    void shouldCreateContributingGuide(@TempDir final Path projectDirectory) {
        assertNotNull(GitUnits.initializeRepository(projectDirectory));
    }

}
