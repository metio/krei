package wtf.metio.krei.model.vcs.git;

import org.immutables.value.Value;
import wtf.metio.krei.template.GitIgnoreTemplates;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Configuration of a .gitignore file.
 */
@Value.Immutable
public interface GitIgnore {

    //region Builders
    static ImmutableGitIgnore.Builder builder() {
        return ImmutableGitIgnore.builder().path(Paths.get(".gitignore"));
    }

    static GitIgnore of(final GitIgnoreTemplates template) {
        return builder().addLines(template.ignores()).build();
    }

    default GitIgnore and(final GitIgnoreTemplates template) {
        return builder().from(this).addLines(template.ignores()).build();
    }
    //endregion

    Optional<Path> path();

    List<String> lines();

}
