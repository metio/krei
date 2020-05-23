package wtf.metio.krei.unit.file;

import wtf.metio.krei.model.License;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.Waiver;

import java.nio.file.Path;

public final class LicenseUnits {

    public static Unit createLicense(final Path path, final License license) {
        return Unit.builder()
                .id("urn:krei:license:file:" + license.id())
                .addExec("cat", ">", path.toAbsolutePath().toString(), "<<", "EOL\n", license.text() + "\n", "EOL")
                .execRaw(true)
                .build();
    }

    public static Unit createWaver(Path path, final Waiver waiver, final String projectName) {
        final var text = String.format(waiver.text(), projectName);
        return Unit.builder()
                .id("urn:krei:license:waiver:" + waiver.id())
                .addExec("cat", ">", path.toAbsolutePath().toString(), "<<", "EOL\n", text + "\n", "EOL")
                .execRaw(true)
                .build();
    }

    public static Unit signWaiver(final Path path) {
        return Unit.builder()
                .id("urn:krei:license:waiver:sign")
                .addExec("gpg2", "--no-version", "--armor", "--sign", path.toAbsolutePath().toString())
                .build();
    }

    public static Unit readmeSection(final Path readme, final License license) {
        return FileUnits.appendFile(readme, license.readme());
    }

    private LicenseUnits() {
        // factory class
    }

}
