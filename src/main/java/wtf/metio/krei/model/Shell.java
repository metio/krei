package wtf.metio.krei.model;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface Shell {

    //region Builders
    static ImmutableShell.Builder builder() {
        return ImmutableShell.builder();
    }

    static Shell of(final String... exec) {
        return builder().addExec(exec).build();
    }

    static Shell raw(final String... exec) {
        return builder().addExec(exec).execRaw(true).build();
    }
    //endregion

    /**
     * List of commands/arguments to execute.
     */
    List<String> exec();

    /**
     * Whether {@link #exec()} should be taken as-is (true), or whitespace delimited (false/default)
     */
    Optional<Boolean> execRaw();

}
