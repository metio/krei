/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.vcs.git;

import org.immutables.value.Value;

import java.util.Map;

/**
 * Configuration of a git config file.
 */
@Value.Immutable
public interface GitConfig {

    //region Builders
    static ImmutableGitConfig.Builder builder() {
        return ImmutableGitConfig.builder();
    }

    static GitConfig of(final String key, final String value) {
        return builder().putEntry(key, value).build();
    }
    //endregion

    Map<String, String> entry();

}
