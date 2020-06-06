/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface Task {

    //region Builders
    static ImmutableTask.Builder builder() {
        return ImmutableTask.builder();
    }

    static Task of(final Action action) {
        return builder().action(action).build();
    }
    //endregion

    Action action();

    Optional<Check> check();

}
