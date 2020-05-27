package wtf.metio.krei.model.vcs.git;

import org.immutables.value.Value;

/**
 * Configuration of a git remote.
 *
 * @see <a href="https://git-scm.com/docs/git-remote">git-scm.com/docs/git-remote</a>
 */
@Value.Immutable
public interface GitRemote {

    //region Builders
    static ImmutableGitRemote.Builder builder() {
        return ImmutableGitRemote.builder();
    }

    static GitRemote github(final String repo) {
        return of("origin", String.format("git@github.com:%s.git", repo));
    }

    static GitRemote of(final String name, final String url) {
        return ImmutableGitRemote.of(name, url);
    }
    //endregion

    @Value.Parameter
    String name();

    @Value.Parameter
    String url();

}
