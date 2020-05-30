/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.vcs.VCS;

import java.nio.file.Path;
import java.util.function.Function;

public final class VCSHandler implements Function<VCS, Unit> {

    private final Path projectDirectory;

    VCSHandler(final Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public Unit apply(final VCS vcs) {
        return Unit.builder().build();
    }

}
