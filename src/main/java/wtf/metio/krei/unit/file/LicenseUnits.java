package wtf.metio.krei.unit.file;

import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.template.LicenseTemplates;

import java.nio.file.Path;

public final class LicenseUnits {

    public static Unit createLicense(final Path path, final LicenseTemplates license) {
        return createLicense(path, license.license());
    }

    public static Unit createLicense(final Path path, final String text) {
        return Unit.builder()
                .id("urn:krei:license:file:" + path.toAbsolutePath())
                .action(FileActions.appendFile(path, text))
                .build();
    }

    public static Unit createWaiver(final Path path, final LicenseTemplates license, final String projectName) {
        final var text = license.waiver(projectName);
        return createWaiver(path, text);
    }

    public static Unit createWaiver(final Path path, final String text) {
        return Unit.builder()
                .id("urn:krei:license:waiver:" + path.toAbsolutePath())
                .action(FileActions.appendFile(path, text))
                .build();
    }

    public static Unit signWaiver(final Path path) {
        return Unit.builder()
                .id("urn:krei:license:waiver:sign")
                .addExec("gpg2", "--no-version", "--armor", "--sign", path.toAbsolutePath().toString())
                .build();
    }

    public static Unit readmeSection(final Path readme, final LicenseTemplates license) {
        return FileUnits.appendFile(readme, license.readme());
    }

    private LicenseUnits() {
        // factory class
    }

}
