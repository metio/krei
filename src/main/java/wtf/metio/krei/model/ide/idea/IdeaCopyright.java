package wtf.metio.krei.model.ide.idea;

import org.immutables.value.Value;

import java.util.Optional;

/**
 * Configuration of a Idea copyright settings.
 */
@Value.Immutable
public interface IdeaCopyright {

    //region Builders
    static ImmutableIdeaCopyright.Builder builder() {
        return ImmutableIdeaCopyright.builder();
    }

    static IdeaCopyright of() {
        return builder().manage(true).build();
    }
    //endregion

    Optional<Boolean> manage();

}
