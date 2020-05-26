package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.License;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.unit.file.FileUnits;
import wtf.metio.krei.unit.idea.IdeaUnits;

import java.nio.file.Path;

public final class MetioIdea {

    public static Unit construct(final Path projectDirectory, final String projectName) {
        final var ideaDirectory = projectDirectory.resolve(".idea");
        final var copyrightDirectory = ideaDirectory.resolve("copyright");

        return Unit.builder()
                .id("urn:krei:metio:idea")
                .addRequires(FileUnits.createDirectory(copyrightDirectory))
                .addRequires(IdeaUnits.configureCopyrightProfile(copyrightDirectory, projectName))
                .addRequires(IdeaUnits.configureCopyright(copyrightDirectory, projectName, License.CC0))
                .build();
    }

    private MetioIdea() {
        // factory class
    }

}
