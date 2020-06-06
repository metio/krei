/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model;

import org.immutables.value.Value;
import wtf.metio.krei.unit.file.FileUnits;

import java.nio.file.Path;
import java.util.List;

/**
 * Groups multiple {@link Unit}s together.
 */
@Value.Immutable
public interface Project {

    //region Builders
    static ImmutableProject.Builder builder() {
        return ImmutableProject.builder();
    }

    static ImmutableProject.Builder builder(final Path projectDirectory) {
        return builder().addUnits(FileUnits.createDirectory(projectDirectory));
    }
    //endregion

    List<Unit> units();

}
