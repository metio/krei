package wtf.metio.krei.action.gitea;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface CreateLabelOption {

    //region Builders
    static ImmutableCreateLabelOption.Builder builder() {
        return ImmutableCreateLabelOption.builder();
    }

    static CreateLabelOption of(final String name, final String color) {
        return ImmutableCreateLabelOption.of(name, color);
    }
    //endregion

    @Value.Parameter
    @JsonProperty
    String color();

    Optional<String> description();

    @Value.Parameter
    @JsonProperty
    String name();

}
