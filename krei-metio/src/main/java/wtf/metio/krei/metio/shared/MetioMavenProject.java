/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.metio.shared;

import wtf.metio.krei.model.ProjectConfig;

import java.util.List;

import static wtf.metio.krei.template.GitIgnoreTemplates.MAVEN;

public final class MetioMavenProject {

    public static ProjectConfig configure(final String projectName) {
        return ProjectConfig.builder(projectName)
                .license(MetioLicense.configure(projectName))
                .vcs(MetioGit.configure(List.of(MAVEN.ignores())))
                .addIde(MetioIdea.configure())
                .community(MetioReadme.configure(projectName))
                .build();
    }

    private MetioMavenProject() {
        // factory class
    }

}