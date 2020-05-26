package wtf.metio.krei.action.gitignore;

import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.model.Action;
import wtf.metio.krei.template.Templates;

import java.nio.file.Path;

public final class GitignoreActions {

    public static Action ignore(final Path gitignoreFile, final String file) {
        final var content = Templates.template("gitignore", file + ".gitignore");
        return FileActions.appendFile(gitignoreFile, content);
    }

    private GitignoreActions() {
        // factory class
    }

}
