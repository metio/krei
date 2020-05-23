package wtf.metio.krei.unit.file;

import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

public final class ReadmeUnits {

    public static Unit createReadme(final Path path) {
        return Unit.builder()
                .id("urn:krei:readme:create")
                .addExec("touch", path.toAbsolutePath().toString())
                .build();
    }

    private ReadmeUnits() {
        // factory class
    }

}
