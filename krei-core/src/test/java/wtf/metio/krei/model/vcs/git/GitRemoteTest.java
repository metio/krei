/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.model.vcs.git;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GitRemote")
class GitRemoteTest {

    @DisplayName("create GitRemote")
    @Test
    void shouldCreateGitRemote() {
        assertNotNull(GitRemote.builder().name("name").url("url").build());
    }

    @DisplayName("create GitRemote with shorthand")
    @Test
    void shouldCreateGitRemoteWithShorthand() {
        assertNotNull(GitRemote.of("name", "url"));
    }

    @DisplayName("create GitRemote for GitHub")
    @Test
    void shouldCreateGitRemoteForGitHub() {
        assertNotNull(GitRemote.github("user/repo"));
    }

}
