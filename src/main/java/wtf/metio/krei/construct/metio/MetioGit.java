package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.vcs.git.Git;
import wtf.metio.krei.model.vcs.git.GitConfig;
import wtf.metio.krei.model.vcs.git.GitIgnore;

import java.util.List;

public final class MetioGit {

    public static Git configure(final List<String> ignores) {
        return Git.builder()
                .config(GitConfig.of("user.email", "seb@hoß.de"))
                .ignore(GitIgnore.builder().addAllLines(ignores).build())
                .build();
    }

    private MetioGit() {
        // factory class
    }

}
