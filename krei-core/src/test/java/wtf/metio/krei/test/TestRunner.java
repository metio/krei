/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.test;

import wtf.metio.krei.model.Action;
import wtf.metio.krei.model.Task;
import wtf.metio.krei.model.Unit;

import static org.junit.jupiter.api.Assertions.*;

public final class TestRunner {

    public static void assertUnit(final Unit unit) {
        assertAction(unit.task().map(Task::action)
                .orElse(() -> -1));
    }

    public static void assertTask(final Task task) {
        assertAction(task.action());
    }

    public static void assertAction(final Action action) {
        final var result = action.call();
        assertAll(
                () -> assertTrue(result >= 0, "action could not be called"),
                () -> assertEquals(0, result, "run did not succeed")
        );
    }

    private TestRunner() {
        // factory class
    }

}
