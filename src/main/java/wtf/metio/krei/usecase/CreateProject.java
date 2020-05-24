package wtf.metio.krei.usecase;

import wtf.metio.krei.model.Project;

import java.io.PrintStream;
import java.nio.file.Path;

public final class CreateProject {

    public static void execute(final Project project, final Path projectDirectory) {
        Executor.execute(project, CreateProject::execute);
    }

    private static void execute(final String exec) {
        // &#10; in HEADER
    }

    private CreateProject() {
        // entry point
    }

}
