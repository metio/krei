package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.community.Community;
import wtf.metio.krei.model.community.readme.Readme;
import wtf.metio.krei.model.community.readme.ReadmeSection;

public final class MetioReadme {

    public static Community configure(final String projectName) {
        return Readme.builder(projectName)
                .addSection(ReadmeSection.of("Usage", 2))
                .addSection(ReadmeSection.of("Alternatives", 2))
                .addSection(ReadmeSection.of("License", 2))
                .addSection(ReadmeSection.of("Mirrors", 2))
                .build();
    }

    private MetioReadme() {
        // factory class
    }

}
