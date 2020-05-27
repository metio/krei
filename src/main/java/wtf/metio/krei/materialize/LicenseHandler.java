package wtf.metio.krei.materialize;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.license.License;
import wtf.metio.krei.model.license.Waiver;
import wtf.metio.krei.unit.file.LicenseUnits;

import java.nio.file.Path;
import java.util.function.Function;

public final class LicenseHandler implements Function<License, Unit> {

    private final Path projectDirectory;

    LicenseHandler(final Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public Unit apply(final License license) {
        final var licenseFile = license.licenseFile()
                .map(projectDirectory::resolve)
                .orElse(projectDirectory.resolve("LICENSE"));
        final var builder = Unit.builder()
                .addRequires(LicenseUnits.createLicense(licenseFile, license.license()));
        license.waiver().map(this::handleWaiver).ifPresent(builder::addRequires);
        return builder.build();
    }

    private Unit handleWaiver(final Waiver waiver) {
        final var waiverFile = waiver.waiverFile()
                .map(projectDirectory::resolve)
                .orElse(projectDirectory.resolve("LICENSE"));
        return Unit.builder()
                .addBefore(LicenseUnits.createWaiver(waiverFile, waiver.text()))
                .addAfter(LicenseUnits.signWaiver(waiverFile))
                .build();
    }

}
