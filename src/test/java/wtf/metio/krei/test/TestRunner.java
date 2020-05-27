package wtf.metio.krei.test;

import wtf.metio.krei.model.Action;
import wtf.metio.krei.model.Unit;

import static org.junit.jupiter.api.Assertions.*;

public final class TestRunner {

    public static void assertUnit(final Unit unit) {
        final var result = unit.action()
                .map(Action::call)
                .orElse(-1);
        assertAll(
                () -> assertTrue(result >= 0, "action could not be called"),
                () -> assertEquals(0, result, "run did not succeed")
        );
    }

    private TestRunner() {
        // factory class
    }

}
