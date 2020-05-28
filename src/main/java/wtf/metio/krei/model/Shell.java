/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

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
