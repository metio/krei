/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model;

import org.junit.jupiter.api.Test;
import wtf.metio.krei.model.build.maven.Maven;
import wtf.metio.krei.model.ide.idea.Idea;
import wtf.metio.krei.model.ide.idea.IdeaCodeStyle;
import wtf.metio.krei.model.ide.idea.IdeaCopyright;
import wtf.metio.krei.model.license.License;
import wtf.metio.krei.model.vcs.git.Git;
import wtf.metio.krei.model.vcs.git.GitConfig;
import wtf.metio.krei.model.vcs.git.GitIgnore;
import wtf.metio.krei.model.vcs.git.GitRemote;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static wtf.metio.krei.template.GitIgnoreTemplates.BAZEL;
import static wtf.metio.krei.template.GitIgnoreTemplates.MAVEN;
import static wtf.metio.krei.template.LicenseTemplates.CC0;

class ProjectConfigTest {

    @Test
    void shouldCreateProject() {
        // given
        final var projectName = "krei";
        final var license = License.of(CC0, projectName);
        final var git = Git.builder()
                .ignore(GitIgnore.of(MAVEN).and(BAZEL))
                .config(GitConfig.of("user.email", "seb@hoß.de"))
                .addRemotes(GitRemote.github("metio/krei"))
                .build();
        final var idea = Idea.builder()
                .copyright(IdeaCopyright.of())
                .codeStyle(IdeaCodeStyle.of())
                .build();
        final var maven = Maven.builder()
                .build();

        // when
        final var project = ProjectConfig.builder(projectName)
                .license(license)
                .vcs(git)
                .addIde(idea)
                .build(maven)
                .build();

        // then
        assertNotNull(project);
    }

}
