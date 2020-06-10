/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class GitIgnoreTemplatesTest {

    @DisplayName("waiver file")
    @ParameterizedTest
    @EnumSource(GitIgnoreTemplates.class)
    void shouldCreateGitIgnore(final GitIgnoreTemplates template) {
        // given
        // when
        final var ignores = template.ignores();

        // then
        assertNotNull(ignores);
    }

}
