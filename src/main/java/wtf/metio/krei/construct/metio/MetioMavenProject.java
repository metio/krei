package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.Configuration;
import wtf.metio.krei.model.Project;
import wtf.metio.krei.unit.git.GitIgnoreUnits;

import java.nio.file.Path;
import java.util.List;

public final class MetioMavenProject {

    public static Project create(final Configuration configuration) {
        return Project.builder(configuration.projectDirectory())
                .addUnits(MetioGit.construct(configuration.projectDirectory(), List.of(GitIgnoreUnits::ignoreMavenOutput)))
                .addUnits(MetioLicense.construct(configuration.projectDirectory()))
                .addUnits(MetioWaiver.construct(configuration.projectDirectory(), configuration.projectName()))
                .addUnits(MetioReadme.construct(configuration.projectDirectory(), configuration.projectName()))
                .addUnits(MetioIdea.construct(configuration.projectDirectory(), configuration.projectName()))
                .build();
    }

    private MetioMavenProject() {
        // factory class
    }

}
