package wtf.metio.krei.model.vcs.git;

import org.immutables.value.Value;

import java.util.Map;

/**
 * Configuration of a git config file.
 */
@Value.Immutable
public interface GitConfig {

    //region Builders
    static ImmutableGitConfig.Builder builder() {
        return ImmutableGitConfig.builder();
    }

    static GitConfig of(final String key, final String value) {
        return builder().putEntry(key, value).build();
    }
    //endregion

    Map<String, String> entry();

}
