package wtf.metio.krei.materialize;

import org.immutables.value.Value;
import wtf.metio.krei.model.Project;
import wtf.metio.krei.model.ProjectConfig;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.build.BuildSystem;
import wtf.metio.krei.model.license.License;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value.Immutable
public interface Materialize {

    //region Builders
    static ImmutableMaterialize.Builder builder(final Path projectDirectory) {
        return ImmutableMaterialize.builder().projectDirectory(projectDirectory);
    }

    static Materialize into(final Path projectDirectory) {
        return builder(projectDirectory)
                .addLicenseHandler(new LicenseHandler(projectDirectory))
                .build();
    }
    //endregion

    default Project asProject(final ProjectConfig config) {
        final var builder = Project.builder(projectDirectory());
        config.build().map(this::handleBuild).ifPresent(builder::addAllUnits);
        config.license().map(this::handleLicense).ifPresent(builder::addAllUnits);
        return builder.build();
    }

    private List<Unit> handleBuild(final BuildSystem build) {
        return buildHandler().stream()
                .map(handler -> handler.apply(build))
                .collect(Collectors.toList());
    }

    private List<Unit> handleLicense(final License license) {
        return licenseHandler().stream()
                .map(handler -> handler.apply(license))
                .collect(Collectors.toList());
    }

    Path projectDirectory();

    List<Function<BuildSystem, Unit>> buildHandler();

    List<Function<License, Unit>> licenseHandler();

}
