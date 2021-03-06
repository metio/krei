/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model;

import org.immutables.value.Value;
import wtf.metio.krei.model.build.BuildSystem;
import wtf.metio.krei.model.community.Community;
import wtf.metio.krei.model.ide.IDE;
import wtf.metio.krei.model.license.License;
import wtf.metio.krei.model.vcs.VCS;

import java.util.Optional;
import java.util.Set;

/**
 * Configuration of a project.
 */
@Value.Immutable
public interface ProjectConfig {

    //region Builders
    static ImmutableProjectConfig.Builder builder(final String name) {
        return ImmutableProjectConfig.builder().name(name);
    }
    //endregion

    Set<BuildSystem> buildSystem();

    Set<Community> community();

    Optional<String> description();

    Set<IDE> ide();

    Set<License> license();

    String name();

    Set<VCS> vcs();

}
