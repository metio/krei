/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.community;

import org.immutables.value.Value;

import java.util.Set;

/**
 * Configures community interaction
 */
@Value.Immutable
public interface Community {

    //region Builders
    static ImmutableCommunity.Builder builder() {
        return ImmutableCommunity.builder();
    }

    default Community and(final Community community) {
        return builder().from(this).addCommunity(community).build();
    }
    //endregion

    Set<Community> community();

}
