package wtf.metio.krei.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wtf.metio.krei.construct.metio.MetioMavenProject;
import wtf.metio.krei.model.Configuration;
import wtf.metio.krei.model.Project;
import wtf.metio.krei.test.FileMasking;
import wtf.metio.krei.unit.git.GitUnits;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

class CreateShellScriptTest {

    @DisplayName("produce expected script")
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("shellScripts")
    void shouldProduceExpectedScript(
            final String fileName,
            final Function<Path, Project> project,
            @TempDir final Path projectDirectory) throws IOException {
        // given
        final var file = Paths.get("src/test/resources/wtf/krei/usecase/CreateShellScriptTest/", fileName);
        final var expectedText = Files.readString(file, UTF_8);

        // when
        final var script = projectDirectory.resolve("test.sh");
        CreateShellScript.execute(project.apply(projectDirectory), script);
        final var text = FileMasking.mask(Files.readString(script, UTF_8));

        // then
        assertEquals(expectedText, text);
    }

    static Stream<Arguments> shellScripts() {
        return Stream.of(
                // of("metio.sh", METIO),
                of("empty-git.sh", EMPTY_GIT)
        );
    }

    static final Function<Path, Project> METIO = (Path dir) ->
            MetioMavenProject.create(new Configuration(dir, "test"));
    static final Function<Path, Project> EMPTY_GIT = (Path dir) ->
            Project.builder(dir)
                    .addUnits(GitUnits.initializeRepository(dir))
                    .build();

}
