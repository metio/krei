/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.usecase;

import wtf.metio.krei.model.Project;

import java.io.PrintStream;

public final class DryRun {

    public static void execute(final Project project, final PrintStream print) {
        Executor.execute(project, print::println);
    }

    private DryRun() {
        // entry point
    }

}
