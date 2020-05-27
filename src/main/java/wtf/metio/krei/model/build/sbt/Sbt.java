package wtf.metio.krei.model.build.sbt;

import org.immutables.value.Value;
import wtf.metio.krei.model.build.BuildSystem;

/**
 * Configuration of a sbt project.
 */
@Value.Immutable
public interface Sbt extends BuildSystem {

    //region Builders
    static ImmutableSbt.Builder builder() {
        return ImmutableSbt.builder();
    }
    //endregion

}
