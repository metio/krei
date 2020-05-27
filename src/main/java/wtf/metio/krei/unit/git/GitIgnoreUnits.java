package wtf.metio.krei.unit.git;

import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

import static wtf.metio.krei.template.GitIgnoreTemplates.BAZEL;
import static wtf.metio.krei.template.GitIgnoreTemplates.MAVEN;

public final class GitIgnoreUnits {

    public static Unit ignoreMavenOutput(final Path gitignoreFile) {
        return Unit.builder()
                .id("urn:krei:git:ignore:maven-output")
                .action(FileActions.appendFile(gitignoreFile, MAVEN.ignores()))
                .build();
    }

    public static Unit ignoreBazelDirectories(final Path gitignoreFile) {
        return Unit.builder()
                .id("urn:krei:git:ignore:bazel-directories")
                .action(FileActions.appendFile(gitignoreFile, BAZEL.ignores()))
                .build();
    }

    private GitIgnoreUnits() {
        // factory class
    }

}
