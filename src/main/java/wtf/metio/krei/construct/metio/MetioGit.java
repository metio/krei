package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.unit.git.GitUnits;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MetioGit {

    public static Unit construct(final Path projectDirectory, final List<Function<Path, Unit>> ignores) {
        final var gitDirectory = projectDirectory.resolve(".git");
        final var gitConfigDirectory = gitDirectory.resolve("config");
        final var gitignoreDirectory = projectDirectory.resolve(".gitignore");

        return Unit.builder()
                .id("urn:krei:metio:license")
                .addRequires(GitUnits.initializeRepository(projectDirectory))
                .addRequires(GitUnits.configureUserEmail(gitConfigDirectory, "seb@hoß.de"))
                .addAllRequires(ignores.stream()
                        .map(ignore -> ignore.apply(gitignoreDirectory))
                        .collect(Collectors.toList()))
                .build();
    }

    private MetioGit() {
        // factory class
    }

}
