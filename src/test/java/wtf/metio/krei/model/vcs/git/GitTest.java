package wtf.metio.krei.model.vcs.git;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import wtf.metio.krei.template.LicenseTemplates;
import wtf.metio.krei.test.FileAsserts;
import wtf.metio.krei.test.TestRunner;
import wtf.metio.krei.unit.file.LicenseUnits;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Git")
class GitTest {

    @DisplayName("create Git")
    @Test
    void shouldCreateGit() {
        assertNotNull(Git.builder().build());
    }

    @DisplayName("create Git with ignores")
    @Test
    void shouldCreateGitWithIgnore() {
        assertNotNull(Git.builder()
                .ignore(GitIgnore.builder().addLines("example").build())
                .build());
    }

    @DisplayName("create Git with config")
    @Test
    void shouldCreateGitWithConfig() {
        assertNotNull(Git.builder()
                .config(GitConfig.of("some.key", "with value"))
                .build());
    }

    @DisplayName("create Git with remotes")
    @Test
    void shouldCreateGitWithRemote() {
        assertNotNull(Git.builder()
                .addRemotes(GitRemote.github("user/repo"))
                .build());
    }

    @DisplayName("create custom Git")
    @ParameterizedTest(name = "[{index}]")
    @MethodSource("customGit")
    void shouldBuildCustomGit(final ImmutableGit.Builder builder) {
        assertNotNull(builder.build());
    }

    static Stream<ImmutableGit.Builder> customGit() {
        return Stream.of(
                Git.builder(),
                Git.builder()
                        .addRemotes(GitRemote.github("user/repo"))
                        .config(GitConfig.of("some.key", "with value")),
                Git.builder()
                        .config(GitConfig.of("some.key", "with value"))
                        .ignore(GitIgnore.builder().addLines("example").build())
        );
    }

}
