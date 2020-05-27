package wtf.metio.krei.model.community;

import org.immutables.value.Value;

import java.util.Set;

/**
 * Configures community interaction
 */
@Value.Immutable
public interface Community {

    //region Builders
    static ImmutableCommunity.Builder builder() {
        return ImmutableCommunity.builder();
    }

    default Community and(final Community community) {
        return builder().from(this).addCommunity(community).build();
    }
    //endregion

    Set<Community> community();

}
