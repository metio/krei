package wtf.metio.krei.model;

import org.immutables.value.Value;
import wtf.metio.krei.unit.file.FileUnits;

import java.nio.file.Path;
import java.util.List;

/**
 * Groups multiple {@link Unit}s together.
 */
@Value.Immutable
public interface Project {

    //region Builders
    static ImmutableProject.Builder builder(final Path projectDirectory) {
        return ImmutableProject.builder()
                .addUnits(FileUnits.createDirectory(projectDirectory));
    }
    //endregion

    List<Unit> units();

}
