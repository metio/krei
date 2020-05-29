/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.usecase;

import wtf.metio.krei.model.Project;

import java.nio.file.Path;

public final class CreateProject {

    public static void execute(final Project project) {
        Executor.execute(project, CreateProject::execute);
    }

    private static void execute(final String exec) {
        // &#10; in HEADER
    }

    private CreateProject() {
        // entry point
    }

}
