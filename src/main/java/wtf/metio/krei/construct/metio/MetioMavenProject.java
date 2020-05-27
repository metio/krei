package wtf.metio.krei.construct.metio;

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
