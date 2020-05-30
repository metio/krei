/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.gitea;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface CreateRepoOption {

    //region Builders
    static ImmutableCreateRepoOption.Builder builder() {
        return ImmutableCreateRepoOption.builder();
    }

    static CreateRepoOption of(final String name) {
        return ImmutableCreateRepoOption.of(name);
    }
    //endregion

    /**
     * Whether the repository should be auto-intialized?
     */
    Optional<Boolean> autoInit();

    @Value.Parameter
    @JsonProperty
    String name();

}
