package wtf.metio.krei.unit.git;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.template.gitignore.Bazel;
import wtf.metio.krei.template.gitignore.Maven;

import java.nio.file.Path;

public final class GitIgnoreUnits {

    public static Unit ignoreMavenOutput(final Path path) {
        return Unit.builder()
                .id("urn:krei:git:ignore:maven-output")
                .addExec("cat", ">>", path.toAbsolutePath().toString(), "<<", "EOL\n", Maven.TEXT + "\n", "EOL")
                .execRaw(true)
                .build();
    }

    public static Unit ignoreBazelDirectories(final Path path) {
        return Unit.builder()
                .id("urn:krei:git:ignore:bazel-directories")
                .addExec("cat", ">>", path.toAbsolutePath().toString(), "<<", "EOL\n", Bazel.TEXT + "\n", "EOL")
                .execRaw(true)
                .build();
    }

    private GitIgnoreUnits() {
        // factory class
    }

}
