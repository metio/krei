/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.ide.idea;

import org.immutables.value.Value;

import java.util.Optional;

/**
 * Configuration of a Idea copyright settings.
 */
@Value.Immutable
public interface IdeaCopyright {

    //region Builders
    static ImmutableIdeaCopyright.Builder builder() {
        return ImmutableIdeaCopyright.builder();
    }

    static IdeaCopyright of() {
        return builder().manage(true).build();
    }
    //endregion

    Optional<Boolean> manage();

}
