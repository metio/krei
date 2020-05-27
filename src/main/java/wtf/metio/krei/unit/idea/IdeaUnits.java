package wtf.metio.krei.unit.idea;

import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.template.IdeaTemplates;
import wtf.metio.krei.template.LicenseTemplates;

import java.nio.file.Path;

/**
 * Units that interact with Jetbrains Idea.
 *
 * @see <a href="https://www.jetbrains.com/idea/">Jetbrains Idea</a>
 */
public final class IdeaUnits {

    /**
     * Configures the <code>.gitignore</code> file inside the <code>.idea</code> directory.
     *
     * @param ideaDirectory The .idea directory to use.
     * @return A unit which will configure the .gitignore file.
     */
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

    public static Unit configureCopyright(
            final Path copyrightDirectory,
            final String projectName,
            final LicenseTemplates license) {
        final var matcher = license.matcher(projectName);
        final var header = license.header(projectName);
        return configureCopyright(copyrightDirectory, projectName, matcher, header);
    }

    public static Unit configureCopyright(
            final Path copyrightDirectory,
            final String projectName,
            final String matcher,
            final String header) {
        final var xml = String.format(IdeaTemplates.copyright(), matcher, header, projectName);
        final var file = copyrightDirectory.resolve(projectName + ".xml");
        return Unit.builder()
                .id("urn:krei:idea:config:copyright")
                .action(FileActions.appendFile(file, xml))
                .build();
    }

    private IdeaUnits() {
        // factory class
    }

}
