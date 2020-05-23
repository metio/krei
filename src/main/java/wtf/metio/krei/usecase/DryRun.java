package wtf.metio.krei.usecase;

import wtf.metio.krei.model.Project;
import wtf.metio.krei.model.Unit;

import java.io.PrintStream;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.stream.Stream.of;

public final class DryRun {

    public static void execute(final Project project, final PrintStream print) {
        Executor.execute(project, print::println);
    }

    private DryRun() {
        // entry point
    }

}
