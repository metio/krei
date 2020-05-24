package wtf.metio.krei.unit.intellij;

import wtf.metio.krei.io.Licenses;
import wtf.metio.krei.model.License;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.template.intellij.Copyright;
import wtf.metio.krei.template.intellij.CopyrightProfile;

import java.nio.file.Path;
import java.util.stream.IntStream;

public final class IdeaUnits {

    public static Unit configureCopyrightProfile(final Path copyrightDirectory, final String projectName) {
        final var text = String.format(CopyrightProfile.TEXT, projectName);
        final var copyrightProfile = copyrightDirectory.resolve("profiles_settings.xml").toAbsolutePath().toString();
        return Unit.builder()
                .id("urn:krei:intellij:config:copyright-profile")
                .addExec("cat", ">", copyrightProfile, "<<", "EOL\n", text + "\n", "EOL")
                .execRaw(true)
                .build();
    }

    public static Unit configureCopyright(final Path copyrightDirectory, final String projectName, final License license) {
        final var matcher = String.format(Licenses.matcher(license), projectName);
        final var args = IntStream.range(0, license.sourceHeaderPlaceholders())
                .mapToObj(i -> projectName)
                .toArray(String[]::new);
        final var shortText = String.format(Licenses.header(license), args);
        final var text = String.format(Copyright.TEXT, matcher, shortText, projectName);
        final var copyright = copyrightDirectory.resolve(projectName + ".xml").toAbsolutePath().toString();
        return Unit.builder()
                .id("urn:krei:intellij:config:copyright")
                .addExec("cat", ">", copyright, "<<", "EOL\n", text + "\n", "EOL")
                .execRaw(true)
                .build();
    }

    private IdeaUnits() {
        // factory class
    }

}
