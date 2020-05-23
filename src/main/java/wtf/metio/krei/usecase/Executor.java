package wtf.metio.krei.usecase;

import wtf.metio.krei.model.Project;
import wtf.metio.krei.model.Unit;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Stream.of;

final class Executor {

    static void execute(
            final Project project,
            final Consumer<String> consumer) {
        execute(project, Executor::defaultFormatter, consumer);
    }

    static void execute(
            final Project project,
            final Function<Unit, Stream<String>> formatter,
            final Consumer<String> consumer) {
        project.units().stream()
                .flatMap(unit -> unitLifecycle(unit, formatter))
                .forEach(consumer);
    }

    private static Stream<String> unitLifecycle(final Unit unit, final Function<Unit, Stream<String>> formatter) {
        final var before = executeUnits(unit.before(), formatter);
        final var after = executeUnits(unit.after(), formatter);
        final var requires = executeUnits(unit.requires(), formatter);
        final var wants = executeUnits(unit.wants(), formatter);
        final var exec = formatExec(unit, formatter);
        return of(before, requires, wants, exec, after)
                .flatMap(identity())
                .filter(not(String::isBlank));
    }

    private static Stream<String> executeUnits(final Set<Unit> units, final Function<Unit, Stream<String>> formatter) {
        return units.stream().flatMap(unit -> unitLifecycle(unit, formatter));
    }
    
    private static Stream<String> defaultFormatter(final Unit unit) {
        final var delimiter = unit.execRaw().orElse(Boolean.FALSE)
                ? "" : " ";
        return of(join(delimiter, unit.exec()));
    }

    private static Stream<String> formatExec(final Unit unit, final Function<Unit, Stream<String>> formatter) {
        if (unit.exec().isEmpty()) {
            return Stream.of();
        }
        return formatter.apply(unit);
    }

    private Executor() {
        // entry point
    }

}
