/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.*;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Stream.of;

final class Executor {

    static int execute(final Project project) {
        return execute(project, Executor::defaultExecute);
    }

    static int execute(
            final Project project,
            final Function<Unit, Stream<Integer>> execute) {
        return project.units().stream()
                .flatMap(unit -> unitLifecycle(unit, execute))
                .max(Integer::compareTo)
                .orElse(0);
    }

    private static Stream<Integer> unitLifecycle(final Unit unit, final Function<Unit, Stream<Integer>> execute) {
        final var before = executeUnits(unit.before(), execute);
        final var after = executeUnits(unit.after(), execute);
        final var requires = executeUnits(unit.requires(), execute);
        final var wants = executeUnits(unit.wants(), execute);
        final var exec = executeUnit(unit, execute);
        return of(before, requires, wants, exec, after)
                .flatMap(identity())
                .filter(Objects::nonNull);
    }

    private static Stream<Integer> executeUnits(final Set<Unit> units, final Function<Unit, Stream<Integer>> execute) {
        return units.stream().flatMap(unit -> unitLifecycle(unit, execute));
    }

    private static Stream<Integer> executeUnit(final Unit unit, final Function<Unit, Stream<Integer>> execute) {
        return execute.apply(unit);
    }

    private static Stream<Integer> defaultExecute(final Unit unit) {
        System.out.println(unit.id());
        return unit.task().stream()
                .filter(task -> task.check().map(Check::test).orElse(true))
                .map(Task::action)
                .map(Action::call);
    }

    private Executor() {
        // entry point
    }

}
