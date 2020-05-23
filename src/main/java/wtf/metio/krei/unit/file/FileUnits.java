package wtf.metio.krei.unit.file;

import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

public final class FileUnits {

    public static Unit createDirectory(final Path directory) {
        return Unit.builder()
                .id("urn:krei:file:directory:create")
                .addExec("mkdir", "--parents", directory.toAbsolutePath().toString())
                .build();
    }

    public static Unit changeDirectory(final Path directory) {
        return Unit.builder()
                .id("urn:krei:file:directory:change")
                .addExec("cd", directory.toAbsolutePath().toString())
                .build();
    }

    public static Unit moveFile(final Path source, final Path target) {
        return Unit.builder()
                .id("urn:krei:file:move:" + source.toAbsolutePath().toString())
                .addExec("mv", source.toAbsolutePath().toString(), target.toAbsolutePath().toString())
                .build();
    }

    public static Unit appendFile(final Path path, final String content) {
        return Unit.builder()
                .id("urn:krei:file:append:" + content.hashCode())
                .addExec("cat", ">>", path.toAbsolutePath().toString(), "<<", "EOL\n", content + "\n", "EOL")
                .execRaw(true)
                .build();
    }

    private FileUnits() {
        // factory class
    }

}
