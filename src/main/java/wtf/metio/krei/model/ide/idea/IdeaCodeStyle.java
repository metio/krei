package wtf.metio.krei.model.ide.idea;

import org.immutables.value.Value;

import java.util.Optional;

/**
 * Configuration of a Idea code style settings.
 */
@Value.Immutable
public interface IdeaCodeStyle {

    //region Builders
    static ImmutableIdeaCodeStyle.Builder builder() {
        return ImmutableIdeaCodeStyle.builder();
    }

    static IdeaCodeStyle of() {
        return builder().manage(true).build();
    }
    //endregion

    Optional<Boolean> manage();

}
