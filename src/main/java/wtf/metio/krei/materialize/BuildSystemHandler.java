/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.build.BuildSystem;
import wtf.metio.krei.model.build.maven.Maven;

import java.nio.file.Path;
import java.util.function.Function;

public final class BuildSystemHandler implements Function<BuildSystem, Unit> {

    private final Path projectDirectory;

    BuildSystemHandler(final Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public Unit apply(final BuildSystem buildSystem) {
        if (buildSystem instanceof Maven maven) {
            return new MavenHandler(projectDirectory).apply(maven);
        }
        // Handle unknown build system?
        return Unit.noop();
    }

}
