/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.validate;

import org.immutables.value.Value;
import wtf.metio.krei.materialize.*;
import wtf.metio.krei.model.ProjectConfig;
import wtf.metio.krei.model.build.BuildSystem;
import wtf.metio.krei.model.community.Community;
import wtf.metio.krei.model.ide.IDE;
import wtf.metio.krei.model.license.License;
import wtf.metio.krei.model.vcs.VCS;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

@Value.Immutable
public interface ValidationOptions {

    //region Builders
    static ImmutableValidationOptions.Builder builder() {
        return ImmutableValidationOptions.builder();
    }

    static ImmutableValidationOptions.Builder from(final ValidationOptions options) {
        return ImmutableValidationOptions.builder().from(options);
    }

    static ValidationOptions standard() {
        return builder()
                .addProjectConfigValidators(new ProjectConfigValidator())
                .addBuildSystemValidators(new BuildSystemValidator())
                .addCommunityValidators(new CommunityValidator())
                .addIdeValidators(new IDEValidator())
                .addLicenseValidators(new LicenseValidator())
                .addVcsValidators(new VCSValidator())
                .build();
    }
    //endregion

    List<Function<ProjectConfig, List<ValidationProblem>>> projectConfigValidators();

    List<Function<BuildSystem, List<ValidationProblem>>> buildSystemValidators();

    List<Function<Community, List<ValidationProblem>>> communityValidators();

    List<Function<IDE, List<ValidationProblem>>> ideValidators();

    List<Function<License, List<ValidationProblem>>> licenseValidators();

    List<Function<VCS, List<ValidationProblem>>> vcsValidators();

}
