package wtf.metio.krei.construct.community;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CommunityHealthTest {

    @Test
    void shouldCreateContributingGuide(@TempDir final Path projectDirectory) {
        assertNotNull(CommunityHealth.contributingGuide(projectDirectory));
    }

}
