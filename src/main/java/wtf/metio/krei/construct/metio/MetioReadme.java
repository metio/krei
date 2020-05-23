package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.License;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.unit.file.FileUnits;
import wtf.metio.krei.unit.file.LicenseUnits;
import wtf.metio.krei.unit.file.ReadmeUnits;

import java.nio.file.Path;

public final class MetioReadme {

    public static Unit construct(final Path projectDirectory, final String projectName) {
        final var readme = projectDirectory.resolve("README.md");

        return Unit.builder()
                .id("urn:krei:metio:readme")
                .addRequires(ReadmeUnits.createReadme(readme))
                .addRequires(FileUnits.appendFile(readme, "# " + projectName))
                .addRequires(FileUnits.appendFile(readme, "## Usage"))
                .addRequires(FileUnits.appendFile(readme, "## Alternatives"))
                .addRequires(FileUnits.appendFile(readme, "## License"))
                .addRequires(LicenseUnits.readmeSection(readme, License.CC0))
                .addRequires(FileUnits.appendFile(readme, "## Mirrors"))
                .build();
    }

    private MetioReadme() {
        // factory class
    }

}
