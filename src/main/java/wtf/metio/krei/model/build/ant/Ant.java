package wtf.metio.krei.model.build.ant;

import org.immutables.value.Value;
import wtf.metio.krei.model.build.BuildSystem;

/**
 * Configuration of an Ant project.
 */
@Value.Immutable
public interface Ant extends BuildSystem {

    //region Builders
    static ImmutableAnt.Builder builder() {
        return ImmutableAnt.builder();
    }
    //endregion

}
