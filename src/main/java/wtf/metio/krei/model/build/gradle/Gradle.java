package wtf.metio.krei.model.build.gradle;

import org.immutables.value.Value;
import wtf.metio.krei.model.build.BuildSystem;

/**
 * Configuration of a Gradle project.
 */
@Value.Immutable
public interface Gradle extends BuildSystem {

    //region Builders
    static ImmutableGradle.Builder builder() {
        return ImmutableGradle.builder();
    }
    //endregion

}
