package wtf.metio.krei.model.vcs.git;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GitConfig")
class GitConfigTest {

    @DisplayName("create GitConfig")
    @Test
    void shouldCreateGitConfig() {
        assertNotNull(GitConfig.builder().build());
    }

    @DisplayName("create GitConfig for single key/value pair")
    @Test
    void shouldHaveShorthandConstructor() {
        assertNotNull(GitConfig.of("key", "value"));
    }

    @DisplayName("create custom GitConfig")
    @ParameterizedTest(name = "[{index}]")
    @MethodSource("customGit")
    void shouldBuildCustomGitConfig(final ImmutableGitConfig.Builder builder) {
        assertNotNull(builder.build());
    }

    static Stream<ImmutableGitConfig.Builder> customGit() {
        return Stream.of(
                GitConfig.builder(),
                GitConfig.builder().putEntry("key", "value"),
                GitConfig.builder().putEntry("key", "value").putEntry("other", "entry")
        );
    }

}
