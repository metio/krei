/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.Action;
import wtf.metio.krei.model.Project;
import wtf.metio.krei.model.Unit;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Stream.of;

final class Executor {

    static int execute(final Project project) {
        return execute(project, Executor::defaultExecutor);
    }

    static int execute(
            final Project project,
            final Function<Unit, Stream<Integer>> executor) {
        return project.units().stream()
                .flatMap(unit -> unitLifecycle(unit, executor))
                .max(Integer::compareTo)
                .orElse(0);
    }

    private static Stream<Integer> unitLifecycle(final Unit unit, final Function<Unit, Stream<Integer>> executor) {
        final var before = executeUnits(unit.before(), executor);
        final var after = executeUnits(unit.after(), executor);
        final var requires = executeUnits(unit.requires(), executor);
        final var wants = executeUnits(unit.wants(), executor);
        final var exec = formatExec(unit, executor);
        return of(before, requires, wants, exec, after)
                .flatMap(identity())
                .filter(Objects::nonNull);
    }

    private static Stream<Integer> executeUnits(final Set<Unit> units, final Function<Unit, Stream<Integer>> formatter) {
        return units.stream().flatMap(unit -> unitLifecycle(unit, formatter));
    }

    private static Stream<Integer> defaultExecutor(final Unit unit) {
        return unit.action().stream().map(Action::call);
    }

    private static Stream<Integer> formatExec(final Unit unit, final Function<Unit, Stream<Integer>> formatter) {
        if (unit.exec().isEmpty()) {
            return Stream.of();
        }
        return formatter.apply(unit);
    }

    private Executor() {
        // entry point
    }

}
