/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.git;

import org.eclipse.jgit.api.Git;
import wtf.metio.krei.action.utils.Callables;
import wtf.metio.krei.model.Action;

import java.nio.file.Path;

public final class GitActions {

    public static Action initializeRepository(final Path projectDirectory) {
        return Callables.none(() -> Git.init()
                .setDirectory(projectDirectory.toAbsolutePath().toFile())
                .call());
    }

    private GitActions() {
        // factory class
    }

}
