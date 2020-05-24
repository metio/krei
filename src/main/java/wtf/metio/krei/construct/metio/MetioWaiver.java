package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.License;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.unit.file.FileUnits;
import wtf.metio.krei.unit.file.LicenseUnits;

import java.nio.file.Path;
import java.time.LocalDate;

public final class MetioWaiver {

    public static Unit construct(final Path projectDirectory, final String projectName) {
        final var username = System.getProperty("user.name");
        final var currentDate = LocalDate.now().toString();
        final var authorDirectory = projectDirectory.resolve("AUTHORS");
        final var waiver = authorDirectory.resolve("WAIVER");
        final var signedWaiver = authorDirectory.resolve("WAIVER.asc");
        final var authorWaiver = authorDirectory.resolve(
                String.format("WAIVER-signed-by-%s-%s.asc", username, currentDate));

        return Unit.builder()
                .id("urn:krei:metio:waiver")
                .addRequires(FileUnits.createDirectory(authorDirectory))
                .addRequires(LicenseUnits.createWaver(waiver, License.CC0, projectName))
                .addRequires(LicenseUnits.signWaiver(waiver))
                .addRequires(FileUnits.moveFile(signedWaiver, authorWaiver))
                .build();
    }

    private MetioWaiver() {
        // factory class
    }

}
