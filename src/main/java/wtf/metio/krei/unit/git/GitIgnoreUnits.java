package wtf.metio.krei.unit.git;

import wtf.metio.krei.action.gitignore.GitignoreActions;
import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

public final class GitIgnoreUnits {

    public static Unit ignoreMavenOutput(final Path path) {
        return Unit.builder()
                .id("urn:krei:git:ignore:maven-output")
                .action(GitignoreActions.ignore(path, "maven"))
                .build();
    }

    public static Unit ignoreBazelDirectories(final Path path) {
        return Unit.builder()
                .id("urn:krei:git:ignore:bazel-directories")
                .action(GitignoreActions.ignore(path, "bazel"))
                .build();
    }

    private GitIgnoreUnits() {
        // factory class
    }

}
