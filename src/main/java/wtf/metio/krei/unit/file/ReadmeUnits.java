/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.unit.file;

import wtf.metio.krei.model.Unit;

import java.nio.file.Path;

public final class ReadmeUnits {

    public static Unit createReadme(final Path path) {
        return Unit.builder()
                .id("urn:krei:readme:create")
                .addExec("touch", path.toAbsolutePath().toString())
                .build();
    }

    private ReadmeUnits() {
        // factory class
    }

}
