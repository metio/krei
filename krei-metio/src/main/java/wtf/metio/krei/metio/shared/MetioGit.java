/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.metio.shared;

import wtf.metio.krei.model.vcs.git.Git;
import wtf.metio.krei.model.vcs.git.GitConfig;
import wtf.metio.krei.model.vcs.git.GitIgnore;

import java.util.List;

public final class MetioGit {

    public static Git configure(final List<String> ignores) {
        return Git.builder()
                .config(GitConfig.of("user.email", "sebhoss@pm.me"))
                .ignore(GitIgnore.builder().addAllLines(ignores).build())
                .build();
    }

    private MetioGit() {
        // factory class
    }

}
