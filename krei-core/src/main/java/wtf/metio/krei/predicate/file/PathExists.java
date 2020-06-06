/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.predicate.file;

import wtf.metio.krei.model.Check;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PathExists implements Check {

    private final Path path;

    PathExists(final Path path) {
        this.path = path;
    }

    @Override
    public boolean test() {
        return Files.exists(path);
    }

}
