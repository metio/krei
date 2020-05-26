package wtf.metio.krei.unit.idea;

import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.model.License;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.template.IdeaTemplates;
import wtf.metio.krei.template.LicenseTemplates;

import java.nio.file.Path;
import java.util.stream.IntStream;

public final class IdeaUnits {

    public static Unit configureGitignore(final Path ideaDirectory) {
        final var text = IdeaTemplates.gitignore();
        final var file = ideaDirectory.resolve(".gitignore");
        return Unit.builder()
                .id("urn:krei:idea:config:gitignore")
                .action(FileActions.appendFile(file, text))
                .build();
    }

    public static Unit configureCopyrightProfile(final Path copyrightDirectory, final String projectName) {
        final var text = String.format(IdeaTemplates.copyrightProfile(), projectName);
        final var file = copyrightDirectory.resolve("profiles_settings.xml");
        return Unit.builder()
                .id("urn:krei:idea:config:copyright-profile")
                .action(FileActions.appendFile(file, text))
                .build();
    }

    public static Unit configureCopyright(final Path copyrightDirectory, final String projectName, final License license) {
        final var matcher = String.format(LicenseTemplates.matcher(license), projectName);
        final var args = IntStream.range(0, license.sourceHeaderPlaceholders())
                .mapToObj(i -> projectName)
                .toArray(String[]::new);
        final var shortText = String.format(LicenseTemplates.header(license), args);
        final var text = String.format(IdeaTemplates.copyright(), matcher, shortText, projectName);
        final var file = copyrightDirectory.resolve(projectName + ".xml");
        return Unit.builder()
                .id("urn:krei:idea:config:copyright")
                .action(FileActions.appendFile(file, text))
                .build();
    }

    private IdeaUnits() {
        // factory class
    }

}
