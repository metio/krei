/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import org.immutables.value.Value;
import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.build.BuildSystem;
import wtf.metio.krei.model.community.Community;
import wtf.metio.krei.model.ide.IDE;
import wtf.metio.krei.model.license.License;
import wtf.metio.krei.model.vcs.VCS;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

@Value.Immutable
public interface MaterializeOptions {

    //region Builders
    static ImmutableMaterializeOptions.Builder builder(final Path projectDirectory) {
        return ImmutableMaterializeOptions.builder().projectDirectory(projectDirectory);
    }

    static MaterializeOptions standard(final Path projectDirectory) {
        return builder(projectDirectory)
                .addBuildHandler(new BuildSystemHandler(projectDirectory))
                .addCommunityHandler(new CommunityHandler(projectDirectory))
                .addIdeHandler(new IDEHandler(projectDirectory))
                .addLicenseHandler(new LicenseHandler(projectDirectory))
                .addVcsHandler(new VCSHandler(projectDirectory))
                .build();
    }
    //endregion

    Path projectDirectory();

    List<Function<BuildSystem, Unit>> buildHandler();

    List<Function<Community, Unit>> communityHandler();

    List<Function<IDE, Unit>> ideHandler();

    List<Function<License, Unit>> licenseHandler();

    List<Function<VCS, Unit>> vcsHandler();

}
