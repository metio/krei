package wtf.metio.krei.model;

import org.immutables.value.Value;
import wtf.metio.krei.model.build.BuildSystem;
import wtf.metio.krei.model.community.Community;
import wtf.metio.krei.model.ide.IDE;
import wtf.metio.krei.model.license.License;
import wtf.metio.krei.model.vcs.VCS;

import java.util.List;
import java.util.Optional;

/**
 * Configuration of a project.
 */
@Value.Immutable
public interface ProjectConfig {

    //region Builders
    static ImmutableProjectConfig.Builder builder(final String name) {
        return ImmutableProjectConfig.builder().name(name);
    }
    //endregion

    Optional<BuildSystem> build();

    Optional<Community> community();

    Optional<String> description();

    List<IDE> ide();

    Optional<License> license();

    String name();

    Optional<VCS> vcs();

}
