package wtf.metio.krei.unit.git;

import wtf.metio.krei.model.Shell;
import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

public final class GitUnits {

    public static Unit initializeRepository(final Path path) {
        return Unit.builder()
                .id("urn:krei:git:repo:init")
                .addExec("git", "init", path.toAbsolutePath().toString())
                .build();
    }

    public static Unit configureUserEmail(final Path path, final String email) {
        return Unit.builder()
                .id("urn:krei:git:config:user-email")
                .addExec("git", "config", "--file", path.toAbsolutePath().toString(), "user.email", email)
                .build();
    }

    private GitUnits() {
        // factory class
    }

}
