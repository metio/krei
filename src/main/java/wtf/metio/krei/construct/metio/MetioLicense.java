package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.License;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.unit.file.LicenseUnits;

import java.nio.file.Path;

public final class MetioLicense {

    public static Unit construct(final Path projectDirectory) {
        final var licenseFile = projectDirectory.resolve("LICENSE");

        return Unit.builder()
                .id("urn:krei:metio:license")
                .addRequires(LicenseUnits.createLicense(licenseFile, License.CC0))
                .build();
    }

    private MetioLicense() {
        // factory class
    }

}
