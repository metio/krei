package wtf.metio.krei.gitea;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface CreateRepoOption {

    //region Builders
    static ImmutableCreateRepoOption.Builder builder() {
        return ImmutableCreateRepoOption.builder();
    }

    static CreateRepoOption of(final String name) {
        return ImmutableCreateRepoOption.of(name);
    }
    //endregion

    /**
     * Whether the repository should be auto-intialized?
     */
    Optional<Boolean> autoInit();

    @Value.Parameter
    @JsonProperty
    String name();

}
