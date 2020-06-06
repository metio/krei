/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.idea;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.task.file.FileTasks;
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
        final var path = ideaDirectory.resolve(".gitignore");
        return Unit.builder()
                .id("urn:krei:idea:config:gitignore")
                .task(FileTasks.appendFile(path, text))
                .build();
    }

    public static Unit configureCopyrightProfile(final Path copyrightDirectory, final String projectName) {
        final var text = String.format(IdeaTemplates.copyrightProfile(), projectName);
        final var path = copyrightDirectory.resolve("profiles_settings.xml");
        return Unit.builder()
                .id("urn:krei:idea:config:copyright-profile")
                .task(FileTasks.appendFile(path, text))
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
        final var path = copyrightDirectory.resolve(projectName + ".xml");
        return Unit.builder()
                .id("urn:krei:idea:config:copyright")
                .task(FileTasks.appendFile(path, xml))
                .build();
    }

    private IdeaUnits() {
        // factory class
    }

}
