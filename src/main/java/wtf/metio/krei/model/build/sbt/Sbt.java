/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.build.sbt;

import org.immutables.value.Value;
import wtf.metio.krei.model.build.BuildSystem;

/**
 * Configuration of a sbt project.
 */
@Value.Immutable
public interface Sbt extends BuildSystem {

    //region Builders
    static ImmutableSbt.Builder builder() {
        return ImmutableSbt.builder();
    }
    //endregion

}
