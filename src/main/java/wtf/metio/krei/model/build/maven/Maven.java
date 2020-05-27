package wtf.metio.krei.model.build.maven;

import org.immutables.value.Value;
import wtf.metio.krei.model.build.BuildSystem;

import java.util.Optional;

/**
 * Configuration of a Maven project.
 */
@Value.Immutable
public interface Maven extends BuildSystem {

    //region Builders
    static ImmutableMaven.Builder builder() {
        return ImmutableMaven.builder();
    }
    //endregion

    Optional<MavenParent> parent();

    Optional<MavenProperties> properties();

    Optional<MavenDependencies> dependencies();

}
