package wtf.metio.krei.model.vcs.git;

import org.immutables.value.Value;
import wtf.metio.krei.model.vcs.VCS;

import java.util.List;
import java.util.Optional;

/**
 * Configuration of a git repository.
 */
@Value.Immutable
public interface Git extends VCS {

    //region Builders
    static ImmutableGit.Builder builder() {
        return ImmutableGit.builder();
    }
    //endregion

    Optional<GitIgnore> ignore();

    Optional<GitConfig> config();

    List<GitRemote> remotes();

}
