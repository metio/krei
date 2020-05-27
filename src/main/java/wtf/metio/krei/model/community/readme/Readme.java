package wtf.metio.krei.model.community.readme;

import org.immutables.value.Value;
import wtf.metio.krei.model.community.Community;

import java.util.List;

@Value.Immutable
public interface Readme extends Community {

    //region Builders
    static ImmutableReadme.Builder builder() {
        return ImmutableReadme.builder();
    }

    static ImmutableReadme.Builder builder(final String title) {
        return ImmutableReadme.builder()
                .addSection(ReadmeSection.builder()
                        .titleText(title)
                        .titleLevel(1)
                        .text("TODO: Add a description of your project here")
                        .build());
    }
    //endregion

    List<ReadmeSection> section();

}
