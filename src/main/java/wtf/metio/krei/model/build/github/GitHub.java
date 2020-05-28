/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.build.github;

import org.immutables.value.Value;
import wtf.metio.krei.model.build.BuildSystem;

/**
 * Configuration of an GitHub Actions workflow.
 */
@Value.Immutable
public interface GitHub extends BuildSystem {

    //region Builders
    static ImmutableGitHub.Builder builder() {
        return ImmutableGitHub.builder();
    }
    //endregion

}
