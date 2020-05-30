/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.community.Community;
import wtf.metio.krei.model.community.readme.Readme;

import java.nio.file.Path;
import java.util.function.Function;

public final class CommunityHandler implements Function<Community, Unit> {

    private final Path projectDirectory;

    CommunityHandler(final Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public Unit apply(final Community community) {
        if (community instanceof Readme readme) {
            return new ReadmeHandler(projectDirectory).apply(readme);
        }
        // Handle unknown community?
        return Unit.noop();
    }

}
