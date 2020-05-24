package wtf.metio.krei.unit.file;

import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.io.Licenses;
import wtf.metio.krei.model.License;
import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

public final class LicenseUnits {

    public static Unit createLicense(final Path path, final License license) {
        return Unit.builder()
                .id("urn:krei:license:file:" + license.id())
                .action(FileActions.appendFile(path, Licenses.license(license)))
                .build();
    }

    public static Unit createWaver(final Path path, final License license, final String projectName) {
        final var text = String.format(Licenses.waiver(license), projectName);
        return Unit.builder()
                .id("urn:krei:license:waiver:" + license.id())
                .action(FileActions.appendFile(path, text))
                .build();
    }

    public static Unit signWaiver(final Path path) {
        return Unit.builder()
                .id("urn:krei:license:waiver:sign")
                .addExec("gpg2", "--no-version", "--armor", "--sign", path.toAbsolutePath().toString())
                .build();
    }

    public static Unit readmeSection(final Path readme, final License license) {
        return FileUnits.appendFile(readme, Licenses.readme(license));
    }

    private LicenseUnits() {
        // factory class
    }

}
