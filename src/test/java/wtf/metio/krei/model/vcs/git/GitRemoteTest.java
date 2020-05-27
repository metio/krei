package wtf.metio.krei.model.vcs.git;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GitRemote")
class GitRemoteTest {

    @DisplayName("create GitRemote")
    @Test
    void shouldCreateGitRemote() {
        assertNotNull(GitRemote.builder().name("name").url("url").build());
    }

    @DisplayName("create GitRemote with shorthand")
    @Test
    void shouldCreateGitRemoteWithShorthand() {
        assertNotNull(GitRemote.of("name", "url"));
    }

    @DisplayName("create GitRemote for GitHub")
    @Test
    void shouldCreateGitRemoteForGitHub() {
        assertNotNull(GitRemote.github("user/repo"));
    }

}
