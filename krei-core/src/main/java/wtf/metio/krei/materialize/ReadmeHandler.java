/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.materialize;

import wtf.metio.krei.model.Unit;
import wtf.metio.krei.model.community.readme.Readme;

import java.nio.file.Path;
import java.util.function.Function;

public final class ReadmeHandler implements Function<Readme, Unit> {

    private final Path projectDirectory;

    ReadmeHandler(final Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public Unit apply(final Readme readme) {
        return Unit.builder().id("urn:krei:materialize:readme:" + projectDirectory.toString()).build();
    }

}
