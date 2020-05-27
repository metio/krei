package wtf.metio.krei.model.community.readme;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface ReadmeSection {

    //region Builders
    static ImmutableReadmeSection.Builder builder() {
        return ImmutableReadmeSection.builder();
    }

    static ReadmeSection of(final String title, final int level) {
        return builder()
                .titleText(title)
                .titleLevel(level)
                .build();
    }
    //endregion

    String titleText();

    int titleLevel();

    Optional<String> text();

}
